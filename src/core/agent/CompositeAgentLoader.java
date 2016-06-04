/*******************************************************************************
 * Copyright (C) 2015 Andrei Olaru, Marius-Tudor Benea, Nguyen Thi Thuy Nga, Amal El Fallah Seghrouchni, Cedric Herpson.
 * 
 * This file is part of tATAmI-PC.
 * 
 * tATAmI-PC is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 * 
 * tATAmI-PC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with tATAmI-PC.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package core.agent;

import java.util.Iterator;

import Logging.Log;
import XML.XMLTree.XMLNode;
import components.dev.mobility.ComponentFactory;
import core.agent.AgentComponent.AgentComponentName;
import core.agent.parametric.AgentParameters;
import core.agent.parametric.ParametricComponent;
import core.simulation.AgentCreationData;
import core.simulation.AgentLoader;
import core.simulation.AgentManager;
import core.simulation.PlatformLoader;
import core.util.platformUtils.PlatformUtils;


/**
 * Agent loader for agents based on {@link CompositeAgent}.
 * 
 * @author Andrei Olaru
 */
public class CompositeAgentLoader implements AgentLoader
{
	/**
	 * Name of XML nodes in the scenario representing components.
	 */
	private static final String	COMPONENT_NODE_NAME			= "component";
	/**
	 * The name of the attribute representing the name of the component in the component node.
	 */
	private static final String	COMPONENT_NAME_ATTRIBUTE	= "name";
	/**
	 * The name of the attribute representing the class of the component in the component node. The class may not be
	 * specified, it the component is standard and its class is specified by the corresponding
	 * {@link AgentComponentName} entry.
	 */
	private static final String	COMPONENT_CLASS_ATTRIBUTE	= "classpath";
	/**
	 * The name of nodes containing component parameters.
	 */
	private static final String	PARAMETER_NODE_NAME			= "parameter";
	/**
	 * The name of the attribute of a parameter node holding the name of the parameter.
	 */
	private static final String	PARAMETER_NAME				= "name";
	/**
	 * The name of the attribute of a parameter node holding the value of the parameter.
	 */
	private static final String	PARAMETER_VALUE				= "value";
	/**
	 * The name of the parameter in the {@link AgentParameters} list that corresponds to a component entry.
	 */
	private static final String	COMPONENT_PARAMETER_NAME	= "agent_component";
	
	/**
	 * The constructor does not do any initializations.
	 */
	public CompositeAgentLoader()
	{
		// nothing to do.
	}
	
	@Override
	public String getName()
	{
		return StandardAgentLoaderType.COMPOSITE.toString();
	}
	
	@Override
	public AgentLoader setConfig(XMLNode configuration)
	{
		// no configuration to load
		return this;
	}
	
	@Override
	public boolean preload(AgentCreationData agentCreationData, PlatformLoader platformLoader)
	{
		String logPre = agentCreationData.getAgentName() + ":";
		Iterator<XMLNode> componentIt = agentCreationData.getNode().getNodeIterator(COMPONENT_NODE_NAME);
		while(componentIt.hasNext())
		{
			XMLNode componentNode = componentIt.next();
			String componentName = componentNode.getAttributeValue(COMPONENT_NAME_ATTRIBUTE);
			
			// get component class
			String componentClass = componentNode.getAttributeValue(COMPONENT_CLASS_ATTRIBUTE);
			if(componentClass == null)
			{
				AgentComponent.AgentComponentName component = AgentComponent.AgentComponentName.toComponentName(componentName);
				Log.v("smth", "=========********** " + component);
				if(component != null)
				{
					if(platformLoader != null)
					{
						String recommendedClass = platformLoader.getRecommendedComponentClass(component);
						Log.v("smth", "$$$$$$$$$$" + recommendedClass);
						if(recommendedClass != null)
							componentClass = recommendedClass;
					}
					if(componentClass == null)
						componentClass = component.getClassName();
				}
				else
				{
					Log.e("CompositeAgentLoader", logPre + "Component [" + componentName
							+ "] unknown and component class not specified. Component will not be available.");
					continue;
				}
			}
			if(componentClass == null)
			{
				Log.e("CompositeAgentLoader", logPre + "Component class not specified for component [" + componentName
						+ "]. Component will not be available.");
				continue;
			}

			/*
			if(PlatformUtils.classExists(componentClass))
				Log.v("CompositeAgentLoader", logPre + "component [" + componentName + "] can be loaded");
			else
			{
				Log.e("CompositeAgentLoader", logPre + "Component class [" + componentName + " | " + componentClass
						+ "] not found; it will not be loaded.");
				continue;
			}

			*/

			Log.v("CompositeAgentLoader", logPre + "Trying to isntatiate: [" + componentClass + "] can be loaded");

			AgentComponent component = null;
			try
			{
				//component = (AgentComponent) PlatformUtils.loadClassInstance(this, componentClass, new Object[0]);

				component = ComponentFactory.getInst().getComponent(componentClass, new Object[0]);
				Log.v("CompositeAgentLoader", "component [] created for agent []. pre-loading..." +  componentClass +  agentCreationData.getAgentName());
			} catch(Exception e)
			{
				Log.e("CompositeAgentLoader", "Component [] failed to load; it will not be available for agent []:" +  componentClass +
						agentCreationData.getAgentName() +  PlatformUtils.printException(e));
				continue;
			}
			
			// load component arguments
			AgentComponent.ComponentCreationData componentData = new AgentComponent.ComponentCreationData();
			Iterator<XMLNode> paramsIt = componentNode.getNodeIterator(PARAMETER_NODE_NAME);
			while(paramsIt.hasNext())
			{
				XMLNode param = paramsIt.next();
				componentData.add(param.getAttributeValue(PARAMETER_NAME), param.getAttributeValue(PARAMETER_VALUE));
			}
			if(AgentComponent.AgentComponentName.PARAMETRIC_COMPONENT.componentName().equals(componentName))
				componentData
						.addObject(ParametricComponent.COMPONENT_PARAMETER_NAME, agentCreationData.getParameters());

			if(component == null){
				Log.v("smth", "==========================" + componentClass);
			}
			if(component.preload(componentData, componentNode, agentCreationData.getPackages()))
			{
				agentCreationData.getParameters().addObject(COMPONENT_PARAMETER_NAME, component);
				Log.v("CompositeAgentLoader", "component [] pre-loaded for agent []" +  componentClass +  agentCreationData.getAgentName());
			}
			else
				Log.e("CompositeAgentLoader", "Component [] failed pre-loading step; it will not be available for agent []." +
						componentClass + agentCreationData.getAgentName());
		}
		
		return true;
	}
	
	@Override
	public AgentManager load(AgentCreationData agentCreationData)
	{
		System.out.println("=================================+++Load");
		CompositeAgent agent = new CompositeAgent();
		for(Object componentObj : agentCreationData.getParameters().getObjects(COMPONENT_PARAMETER_NAME))
			agent.addComponent((AgentComponent) componentObj);
		return agent;
	}
}
