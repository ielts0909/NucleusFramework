package com.jcwhatever.nucleus.internal.managed.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.jcwhatever.v1_8_R3.BukkitTester;
import com.jcwhatever.nucleus.NucleusTest;
import com.jcwhatever.nucleus.internal.managed.reflection.ReflectableType.ReflectableTestEnum;
import com.jcwhatever.nucleus.managed.reflection.IReflectedArray;
import com.jcwhatever.nucleus.managed.reflection.IReflectedField;
import com.jcwhatever.nucleus.managed.reflection.IReflectedInstance;
import com.jcwhatever.nucleus.managed.reflection.IReflectedType;
import com.jcwhatever.nucleus.managed.reflection.IReflectedTypeFields;
import com.jcwhatever.nucleus.managed.reflection.IReflection;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReflectedTypeTest {

    private IReflectedType reflectedClass;
    private IReflectedType reflectedEnum;

    @BeforeClass
    public static void beforeClass() {
        NucleusTest.init();
    }

    @Before
    public void before() {
        ReflectableType.staticField = true;

        IReflection reflection = new ReflectionContext(BukkitTester.NMS_TEST_VERSION);
        reflectedClass = reflection.type(ReflectableType.class);
        reflectedEnum = reflection.type(ReflectableTestEnum.class);
    }

    @Test
    public void testGetHandle() throws Exception {

        assertEquals(ReflectableType.class, reflectedClass.getHandle());
    }

    @Test
    public void testGetFields() throws Exception {

        IReflectedTypeFields fields = reflectedClass.getFields(Object.class);

        assertEquals(4, fields.size());


        fields = reflectedClass.getFields(int.class);

        assertEquals(1, fields.size());


        fields = reflectedClass.getFields(String.class);

        assertEquals(1, fields.size());

        fields = reflectedClass.getFields(boolean.class);

        assertEquals(1, fields.size());

        fields = reflectedClass.getFields(ReflectableTestEnum.class);

        assertEquals(1, fields.size());
    }

    @Test
    public void testGetField() throws Exception {

        IReflectedField field = reflectedClass.getField("field1");

        assertTrue(field != null);

        assertEquals("field1", field.getName());

        assertEquals(false, field.isStatic());

        assertEquals(String.class, field.getReflectedType().getHandle());
    }

    @Test
    public void testGetStaticField() throws Exception {

        IReflectedField field = reflectedClass.getField("staticField");

        assertTrue(field != null);

        assertEquals("staticField", field.getName());

        assertEquals(true, field.isStatic());

        assertEquals(boolean.class, field.getReflectedType().getHandle());
    }

    @Test
    public void testGetEnum() throws Exception {

        Object enumConstant2 = reflectedEnum.getEnum("CONSTANT2");

        assertEquals(ReflectableTestEnum.CONSTANT2, enumConstant2);

        try {
            reflectedClass.getEnum("CONSTANT2");
            throw new AssertionError("RuntimeException expected.");
        }
        catch(RuntimeException ignore) {}
    }

    @Test
    public void testGet() throws Exception {

        try {
            assertEquals("string", reflectedClass.get("field1"));
            throw new AssertionError("RuntimeException expected.");
        }
        catch (RuntimeException ignore) {}

        try {
            assertEquals(1, reflectedClass.get("field2"));
            throw new AssertionError("RuntimeException expected.");
        }
        catch (RuntimeException ignore) {}

        try {
            assertEquals(ReflectableTestEnum.CONSTANT2, reflectedClass.get("field3"));
            throw new AssertionError("RuntimeException expected.");
        }
        catch (RuntimeException ignore) {}

        assertEquals(true, reflectedClass.get("staticField"));

    }

    @Test
    public void testSet() throws Exception {

        reflectedClass.set("staticField", false);

        assertEquals(false, ReflectableType.staticField);
    }

    @Test
    public void testConstruct() throws Exception {

        reflectedClass.constructorAlias("constructor");
        reflectedClass.constructorAlias("constructor2", String.class);

        Object instance = reflectedClass.construct("constructor");
        assertTrue(instance instanceof ReflectableType);

        instance = reflectedClass.construct("constructor2", "arg");
        assertTrue(instance instanceof ReflectableType);
    }

    @Test
    public void testConstructReflect() throws Exception {

        reflectedClass.constructorAlias("constructor");
        reflectedClass.constructorAlias("constructor2", String.class);

        IReflectedInstance instance = reflectedClass.constructReflect("constructor");
        assertTrue(instance.getHandle() instanceof ReflectableType);

        instance = reflectedClass.constructReflect("constructor2", "arg");
        assertTrue(instance.getHandle() instanceof ReflectableType);
    }

    @Test
    public void testNewArray() throws Exception {

        IReflectedArray array = reflectedClass.newArray(10);

        assertTrue(array.getHandle() instanceof ReflectableType[]);

        assertEquals(10, ((ReflectableType[])array.getHandle()).length);
    }

    @Test
    public void testNewArray1() throws Exception {

        IReflectedArray array = reflectedClass.newArray(10, 10, 10);

        assertTrue(array.getHandle() instanceof ReflectableType[][][]);

        assertEquals(10, ((ReflectableType[][][])array.getHandle()).length);
    }

    @Test
    public void testReflect() throws Exception {

        ReflectableType instance = new ReflectableType();

        IReflectedInstance reflected = reflectedClass.reflect(instance);

        assertTrue(reflected.getHandle() == instance);
    }

    @Test
    public void testReflectArray() throws Exception {

        ReflectableType[] array = new ReflectableType[1];

        IReflectedArray reflectedArray = reflectedClass.reflectArray(array);

        assertTrue(reflectedArray.getHandle() == array);


        ReflectableType[][][] array2 = new ReflectableType[1][2][2];

        reflectedArray = reflectedClass.reflectArray(array2);

        assertTrue(reflectedArray.getHandle() == array2);
    }

    @Test
    public void testFieldAlias() throws Exception {

        reflectedClass.fieldAlias("a", "field1");

        IReflectedField field = reflectedClass.getField("a");

        assertTrue(field != null);

        assertEquals("field1", field.getName());

        assertEquals(false, field.isStatic());

        assertEquals(String.class, field.getReflectedType().getHandle());
    }

    @Test
    public void testEnumAlias() throws Exception {

        reflectedEnum.enumAlias("a", "CONSTANT2");

        Object enumConstant2 = reflectedEnum.getEnum("a");

        assertEquals(ReflectableTestEnum.CONSTANT2, enumConstant2);

        try {
            reflectedClass.getEnum("CONSTANT2");
            throw new AssertionError("RuntimeException expected.");
        }
        catch(RuntimeException ignore) {}
    }

    @Test
    public void testEnumConst() throws Exception {

        reflectedEnum.enumConst("CONSTANT2");

        Object enumConstant2 = reflectedEnum.getEnum("CONSTANT2");

        assertEquals(ReflectableTestEnum.CONSTANT2, enumConstant2);

        try {
            reflectedClass.getEnum("CONSTANT2");
            throw new AssertionError("RuntimeException expected.");
        }
        catch(RuntimeException ignore) {}
    }

    @Test
    public void testMethod() throws Exception {

        reflectedClass.method("method1");
        reflectedClass.method("method2", String.class);
        reflectedClass.method("staticMethod1");
        reflectedClass.method("staticMethod2", boolean.class);

        reflectedClass.constructorAlias("new");

        // Test with a method that has no parameters.
        IReflectedInstance instance = reflectedClass.constructReflect("new");

        Object obj = instance.invoke("method1");
        assertEquals("string", obj);

        //  Test with parameters
        instance = reflectedClass.constructReflect("new");
        instance.invoke("method2", "test");
        assertEquals("test", ((ReflectableType)instance.getHandle()).field1);

        // Test static method with no parameters
        obj = reflectedClass.invokeStatic("staticMethod1");
        assertEquals(true, obj);

        // Test static method with 1 boolean parameter
        reflectedClass.invokeStatic("staticMethod2", false);
        assertEquals(false, ReflectableType.staticField);
    }

    @Test
    public void testMethodAlias() throws Exception {

        // reset to prevent problems in other tests
        ReflectableType.staticField = true;

        reflectedClass.methodAlias("a", "method1");
        reflectedClass.methodAlias("b", "method2", String.class);
        reflectedClass.methodAlias("c", "staticMethod1");
        reflectedClass.methodAlias("d", "staticMethod2", boolean.class);

        reflectedClass.constructorAlias("new");


        // Test with a method that has no parameters.
        IReflectedInstance instance = reflectedClass.constructReflect("new");

        Object obj = instance.invoke("a");
        assertEquals("string", obj);

        //  Test with parameters
        instance = reflectedClass.constructReflect("new");

        instance.invoke("b", "test");
        assertEquals("test", ((ReflectableType)instance.getHandle()).field1);


        // Test static method with no parameters
        obj = reflectedClass.invokeStatic("c");
        assertEquals(true, obj);


        // Test static method with 1 boolean parameter
        reflectedClass.invokeStatic("d", false);
        assertEquals(false, ReflectableType.staticField);
    }
}