package wtf.pants.stamp.obfuscator.obfuscators;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import wtf.pants.stamp.obfuscator.Obfuscator;
import wtf.pants.stamp.util.Log;
import wtf.pants.stamp.util.ObfUtil;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Spacks
 */
@SuppressWarnings("unchecked")
public class ObfuscatorStrings extends Obfuscator {

    public ObfuscatorStrings() {
        super("Strings", -1);
    }

    /**
     * Searches a MethodNode for strings (LdcInsnNode). It will add the found strings to the List parameter also
     * replacing the Ldc node to get from a string array
     *
     * @param strings    A List that will have strings found added to it
     * @param fieldName  The name of the string array that the Ldc node will be replaced with
     * @param classNode  The node of the class the method is in
     * @param methodNode The method to be searched
     */
    public void searchAndReplaceStrings(List<String> strings, String fieldName, ClassNode classNode, MethodNode methodNode) {
        final InsnList methodInsn = methodNode.instructions;
        final InsnList insnList = new InsnList();

        setInsn(insnList);

        AbstractInsnNode[] insnNodes = methodInsn.toArray();

        for (AbstractInsnNode insn : insnNodes) {
            if (insn instanceof LdcInsnNode) {
                final LdcInsnNode min = (LdcInsnNode) insn;

                //Makes sure the LdcInsnNode's value is a string and not something else
                if (min.cst instanceof String && !min.cst.toString().equals("")) {
                    final String ldcString = min.cst.toString();

                    strings.add(ldcString);
                    final int id = strings.size();

                    //<field name>[<id>]
                    {
                        field(GETSTATIC, classNode.name, fieldName, "[Ljava/lang/String;");
                        pushInt(id - 1);
                        aaload();
                    }

                    continue;
                }
            }
            insnList.add(insn);
        }

        methodNode.instructions = insnList;
    }

    @Override
    public void obfuscate(ClassReader classReader, ClassNode classNode, int pass) {
        try {
            Log.log("Obfuscating strings");

            final String stringsFieldName = ObfUtil.getRandomObfString();
            final List<String> strings = new ArrayList<>();

            List<MethodNode> methodNodes = classNode.methods;

            if ((classNode.access & ACC_INTERFACE) != 0) {
                Log.log("%s is an interface, skipping", classNode.name);
                return;
            }

            classNode.fields.add(new FieldNode(ACC_PRIVATE + ACC_STATIC, stringsFieldName, "[Ljava/lang/String;", null, null));

            methodNodes.forEach(m -> searchAndReplaceStrings(strings, stringsFieldName, classNode, m));

            if (strings.size() == 0) {
                Log.error("Class had no strings!");
                return;
            }

            MethodNode mn = createStringMethod(strings, stringsFieldName, classNode);

            MethodNode clinit = null;

            for (Object method : classNode.methods) {
                if (method instanceof MethodNode) {
                    if (((MethodNode) method).name.equals("<clinit>")) {
                        clinit = (MethodNode) method;
                        break;
                    }
                }
            }

            if (clinit == null) {
                Log.info("Adding <clinit> method");
                classNode.methods.add(mn);
            } else {
                Log.error("Existing <clinit> method found! Inserting after");
                //TODO: No idea if this works need to test it
                clinit.instructions.add(mn.instructions);
                return;
            }

            Log.log("Finished obfuscate strings");
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.out.println(classNode.name);
        }
    }

    /**
     * Creates the method that holds and loads the obfuscated strings
     *
     * @param strings   A list containing the strings found within the class
     * @param fieldName The string array field name
     * @param cn        The class the method is being added to
     * @return Returns a newly created method
     */
    public MethodNode createStringMethod(List<String> strings, String fieldName, ClassNode cn) {
        if (strings.size() == 0) {
            return null;
        }

        final MethodNode methodNode = new MethodNode(ACC_STATIC, "<clinit>", "()V", null, null);
        final InsnList array = methodNode.instructions;

        setInsn(array);

        pushInt(strings.size());
        array.add(new TypeInsnNode(ANEWARRAY, "[I"));
        array.add(new VarInsnNode(ASTORE, 0));

        int placement = 0;

        //adds the encoded strings to the 2d int array
        for (final String string : strings) {
            final char[] charArray = string.toCharArray();

            array.add(new VarInsnNode(ALOAD, 0));
            //Place in array
            pushInt(placement);
            pushInt(charArray.length);
            array.add(new IntInsnNode(NEWARRAY, T_INT));
            dup();

            for (int id = 0; id < charArray.length; id++) {
                final int charId = ((int) charArray[id]) + (placement + 1);

                pushInt(id);
                pushInt(charId);

                array.add(new InsnNode(IASTORE));

                if (id != charArray.length - 1) {
                    dup();
                } else {
                    aastore();
                }
            }

            //Log.log("Added: '" + string.replace("%", "%%") + "'");

            placement++;
        }

        addDecoder(fieldName, array, cn);

        return methodNode;
    }

    /**
     * Generates the method that loads the strings into a string array
     * TODO: This needs to be redone more eye friendly
     *
     * @param fieldName String array field name
     * @param array     The instructions of the method we created
     * @param cn        The Class we're adding this to
     */
    private void addDecoder(String fieldName, InsnList array, ClassNode cn) {
        //Initializes 'static String[] strings'
        array.add(new VarInsnNode(ALOAD, 0));
        array.add(new InsnNode(ARRAYLENGTH));
        array.add(new TypeInsnNode(ANEWARRAY, "java/lang/String"));
        array.add(new FieldInsnNode(PUTSTATIC, cn.name, fieldName, "[Ljava/lang/String;"));


        //Int offset
        array.add(new InsnNode(ICONST_1));
        array.add(new VarInsnNode(ISTORE, 1));

        //for (int[] obfArray : obfStringArray)
        array.add(new VarInsnNode(ALOAD, 0));
        array.add(new VarInsnNode(ASTORE, 2));
        array.add(new VarInsnNode(ALOAD, 2));
        array.add(new InsnNode(ARRAYLENGTH));
        array.add(new VarInsnNode(ISTORE, 3));
        array.add(new InsnNode(ICONST_0));
        array.add(new VarInsnNode(ISTORE, 4));

        //
        LabelNode l4 = new LabelNode();
        array.add(new LabelNode(l4.getLabel()));
        array.add(new FrameNode(Opcodes.F_FULL, 5, new Object[]{"[[I", Opcodes.INTEGER, "[[I", Opcodes.INTEGER, Opcodes.INTEGER}, 0, new Object[]{}));
        array.add(new VarInsnNode(ILOAD, 4));
        array.add(new VarInsnNode(ILOAD, 3));
//
        LabelNode l5 = new LabelNode();
        array.add(new JumpInsnNode(IF_ICMPGE, l5));
//        //IF_ICMPGE L5
//
        array.add(new VarInsnNode(ALOAD, 2));
        array.add(new VarInsnNode(ILOAD, 4));
        array.add(new InsnNode(AALOAD));
        array.add(new VarInsnNode(ASTORE, 5));
//
//
        //String s = ""
        array.add(new LdcInsnNode(""));
        array.add(new VarInsnNode(ASTORE, 6));

        //for (int i = 0; i < obfArray.length; i++)
        array.add(new InsnNode(ICONST_0));
        array.add(new VarInsnNode(ISTORE, 7));
//
        LabelNode l8 = new LabelNode();
        array.add(new LabelNode(l8.getLabel()));
        array.add(new FrameNode(Opcodes.F_APPEND, 3, new Object[]{"[I", "java/lang/String", Opcodes.INTEGER}, 0, null));
        array.add(new VarInsnNode(ILOAD, 7));
        array.add(new VarInsnNode(ALOAD, 5));
        array.add(new InsnNode(ARRAYLENGTH));

        LabelNode l9 = new LabelNode();
        array.add(new JumpInsnNode(IF_ICMPGE, l9));
//        //IF_ICMPGE L9
//
        array.add(new TypeInsnNode(NEW, "java/lang/StringBuilder"));
        array.add(new InsnNode(DUP));
        array.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false));
        array.add(new VarInsnNode(ALOAD, 6));
        array.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
        array.add(new VarInsnNode(ALOAD, 5));
        array.add(new VarInsnNode(ILOAD, 7));
        array.add(new InsnNode(IALOAD));
        array.add(new VarInsnNode(ILOAD, 1));
        array.add(new InsnNode(ISUB));
        array.add(new InsnNode(I2C));
        array.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false));
        array.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false));
        array.add(new VarInsnNode(ASTORE, 6));
//
        array.add(new IincInsnNode(7, 1));
        array.add(new JumpInsnNode(GOTO, l8));

        array.add(new LabelNode(l9.getLabel()));
        array.add(new FrameNode(F_CHOP, 1, null, 0, null));
        field(GETSTATIC, cn.name, fieldName, "[Ljava/lang/String;");
        array.add(new IincInsnNode(1, 1));
        array.add(new VarInsnNode(ILOAD, 1));
        pushInt(2);
        isub();
        aload(6);
        aastore();


        array.add(new IincInsnNode(4, 1));
        array.add(new JumpInsnNode(GOTO, l4));

        array.add(new LabelNode(l5.getLabel()));
        array.add(new FrameNode(F_FULL, 0, new Object[]{}, 0, new Object[]{}));

        array.add(new InsnNode(RETURN));
    }
}
