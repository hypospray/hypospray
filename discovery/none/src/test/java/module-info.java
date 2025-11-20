import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryFilter;

open module eu.jbeernink.hypospray.discovery.none.test {
	requires jakarta.cdi;
	requires org.junit.jupiter.api;
	requires eu.jbeernink.hypospray.discovery.none;
	requires eu.jbeernink.hypospray.discovery.scanner;

	uses BeanDiscoveryFilter;
}