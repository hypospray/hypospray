package eu.jbeernink.hypospray.core.inject.spi.factory.cases;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class TestClass implements TestInterface {

	private String field1;
	private Object field2;

	public TestClass() {
	}

	public TestClass(String string) {
	}

	public void postConstruct() {
	}

	public void preDestroy() {
	}

	public static String produceString() {
		return "Hello, world!";
	}

	public void setField1(String field1) {
		this.field1 = field1;
	}

	public void setField2(Object field2) {
		this.field2 = field2;
	}
}
