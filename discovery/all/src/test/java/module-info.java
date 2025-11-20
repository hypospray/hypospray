import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryFilter;

open module eu.jbeernink.hypospray.discovery.all.test {
	requires org.junit.jupiter.api;
	requires eu.jbeernink.hypospray.discovery.all;
	requires eu.jbeernink.hypospray.discovery.scanner;

	uses BeanDiscoveryFilter;
}