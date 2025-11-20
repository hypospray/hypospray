package eu.jbeernink.hypospray.xml.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.xml.BeansConfigReader;
import eu.jbeernink.hypospray.xml.model.BeanConfig;

@DisplayName("BeansXmlReader")
public class BeansConfigReaderTest {

	private BeansConfigReader beansConfigReader;

	@BeforeEach
	void setup() {
		beansConfigReader = BeansConfigReader.newInstance();
	}

	@Test
	@DisplayName("readConfig(URI) with empty beans.xml file, returns empty optional.")
	void readConfig_withEmptyBeansXmlFile_returnsEmptyOptional() throws Exception {
		var configPath = Paths.get(getClass().getResource("/empty-beans.xml").toURI());

		Optional<BeanConfig> beanConfig = beansConfigReader.readConfig(configPath);

		assertEquals(Optional.empty(), beanConfig);
	}

	@Test
	@DisplayName("readConfig(URI) with non-empty beans.xml file, returns bean config.")
	void readConfig_withNonEmptyBeansXmlFile_returnsExpectedBeanConfig() throws Exception {
		var configPath = Paths.get(getClass().getResource("/non-empty-beans.xml").toURI());

		Optional<BeanConfig> beanConfig = beansConfigReader.readConfig(configPath);

		assertEquals(Optional.of(new BeanConfig("all")), beanConfig);
	}

}
