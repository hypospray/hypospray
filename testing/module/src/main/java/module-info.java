module eu.jbeernink.hypospray.testing.module {
	requires jakarta.cdi;

	exports eu.jbeernink.hypospray.testing.module.exported;

	opens eu.jbeernink.hypospray.testing.module.open;
	opens eu.jbeernink.hypospray.testing.module.open.cdi to eu.jbeernink.hypospray.core;
}