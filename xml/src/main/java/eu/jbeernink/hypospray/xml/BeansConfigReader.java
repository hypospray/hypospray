package eu.jbeernink.hypospray.xml;

import static java.lang.System.Logger.Level.DEBUG;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.System.Logger;
import java.nio.file.Path;
import java.util.Optional;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import eu.jbeernink.hypospray.xml.model.BeanConfig;

public final class BeansConfigReader {

	private static final Logger logger = System.getLogger(BeansConfigReader.class.getName());

	private BeansConfigReader() {
	}

	public Optional<BeanConfig> readConfig(Path path) throws IOException {
		return readConfig(newInputStream(path));
	}

	public Optional<BeanConfig> readConfig(InputStream in) throws IOException {
		try (InputStreamReader reader = new InputStreamReader(in, UTF_8)) {
			return readConfig(reader);
		}
	}

	public Optional<BeanConfig> readConfig(Reader reader) throws IOException {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader eventReader = null;
		try {
			try {
				eventReader = inputFactory.createXMLEventReader(reader);

				if (eventReader.hasNext()) {
					XMLEvent startEvent = eventReader.nextEvent();
					if (startEvent.isStartDocument() && isNextElementStartOfTag(eventReader, "beans")) {
						var beansTag = eventReader.nextEvent().asStartElement();

						Attribute beanDiscoveryModeAttribute = beansTag.getAttributeByName(QName.valueOf("bean-discovery-mode"));

						if (beanDiscoveryModeAttribute != null) {
							return Optional.of(new BeanConfig(beanDiscoveryModeAttribute.getValue()));
						}
					}
				}
			} finally {
				if (eventReader != null) {
					eventReader.close();
				}
			}

		} catch (XMLStreamException e) {
			throw new IOException(e);
		}

		return Optional.empty();
	}

	private static boolean isNextElementStartOfTag(XMLEventReader eventReader, String tagName) throws XMLStreamException {
		try {
			if (eventReader.hasNext() && eventReader.peek() instanceof StartElement startElement) {
				return startElement.getName()
				                   .equals(QName.valueOf("{https://jakarta.ee/xml/ns/jakartaee}%s".formatted(tagName))) ||
				       startElement.getName().equals(QName.valueOf(tagName));
			}
		} catch (XMLStreamException e) {
			if (e.getMessage().contains("Unexpected EOF ")) {
				logger.log(DEBUG, "EOF reached, treating beans.xml as empty.", e);
			}
		}

		return false;
	}

	public Optional<BeanConfig> readConfig(String contents) throws IOException {
		return readConfig(new StringReader(contents));
	}

	public static BeansConfigReader newInstance() {
		return new BeansConfigReader();
	}
}
