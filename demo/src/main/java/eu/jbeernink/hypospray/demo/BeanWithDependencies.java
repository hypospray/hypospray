package eu.jbeernink.hypospray.demo;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import eu.jbeernink.hypospray.demo.qualifier.Qualified;

@Dependent
public class BeanWithDependencies {

	private final ScopedBean scopedBean;
	private UnscopedBean unscopedBean;

	@Inject
	@Qualified
	private Object qualifiedBean;

	private Object anotherBean;

	@Inject
	BeanWithDependencies(ScopedBean scopedBean, UnscopedBean unscopedBean) {
		this.scopedBean = scopedBean;
		this.unscopedBean = unscopedBean;
		StackWalker.getInstance().forEach(System.err::println);
	}


	@Inject
	private void setAnotherBean(@Qualified Object anotherBean) {
		this.anotherBean = anotherBean;
	}

	@Override
	public String toString() {
		return "BeanWithDependencies{" + "scopedBean=" + scopedBean + "(" + scopedBean.getClass() + ")" +
		       ", unscopedBean=" + unscopedBean + ", qualifiedBean=" + qualifiedBean + ", anotherBean=" + anotherBean + '}';
	}
}
