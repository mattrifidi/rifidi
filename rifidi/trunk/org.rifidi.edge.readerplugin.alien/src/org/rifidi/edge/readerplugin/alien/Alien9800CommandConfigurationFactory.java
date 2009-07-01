/**
 * 
 */
package org.rifidi.edge.readerplugin.alien;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rifidi.edge.core.sensors.commands.AbstractCommandConfiguration;
import org.rifidi.edge.core.sensors.commands.AbstractCommandConfigurationFactory;
import org.rifidi.edge.readerplugin.alien.commands.AlienGetTagListCommandConfiguration;

/**
 * The CommandConfigurationFactory for an Alien9800 reader
 * 
 * @author Jochen Mader - jochen@pramari.com
 * 
 */
public class Alien9800CommandConfigurationFactory extends
		AbstractCommandConfigurationFactory {

	/**
	 * A map between IDs of command configutions factories and the classes of
	 * the commandconfigurations factories
	 */
	private Map<String, Class<?>> factoryIdToClass;
	/** The gloabaly unique name for this factory */
	public static final String uniqueID = "Alien9800CommandConfigurationFactory";

	/**
	 * Constructor
	 */
	public Alien9800CommandConfigurationFactory() {
		super();
		factoryIdToClass = new HashMap<String, Class<?>>();
		factoryIdToClass.put("Alien9800-GetTagList",
				AlienGetTagListCommandConfiguration.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.configuration.AbstractMultiServiceFactory#customInit(java.
	 * lang.Object)
	 */
	@Override
	public void customInit(Object instance) {
		if (instance instanceof AbstractCommandConfiguration<?>) {
			AbstractCommandConfiguration<?> cc = (AbstractCommandConfiguration<?>) instance;
			Set<String> intefaces = new HashSet<String>();
			intefaces.add(AbstractCommandConfiguration.class.getName());
			cc.register(getContext(), intefaces);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.configuration.AbstractMultiServiceFactory#getFactoryIDToClass
	 * ()
	 */
	@Override
	public Map<String, Class<?>> getFactoryIDToClass() {
		return new HashMap<String, Class<?>>(factoryIdToClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.configuration.ServiceFactory#getFactoryIDs()
	 */
	@Override
	public List<String> getFactoryIDs() {
		return new ArrayList<String>(factoryIdToClass.keySet());
	}

	/**
	 * @return the ID of the factory these commands work with
	 */
	@Override
	public String getReaderFactoryID() {
		return Alien9800ReaderFactory.FACTORY_ID;
	}

}