/*******************************************************************************
 * Copyright (C) 2013 Andrei Olaru, Marius-Tudor Benea, Nguyen Thi Thuy Nga, Amal El Fallah Seghrouchni, Cedric Herpson.
 * 
 * This file is part of tATAmI-PC.
 * 
 * tATAmI-PC is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 * 
 * tATAmI-PC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with tATAmI-PC.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package tatami.core.util.platformUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Vector;

import XML.XMLTree.XMLNode;

/**
 * Platform-related functionality. All functions should be static.
 * 
 * @author Andrei Olaru
 * 
 */
public class PlatformUtils
{
	/**
	 * The name of nodes containing parameters.
	 */
	private static final String	PARAMETER_NODE_NAME	= "parameter";
	/**
	 * The name of the attribute of a parameter node holding the name of the parameter.
	 */
	private static final String	PARAMETER_NAME		= "name";
	/**
	 * The name of the attribute of a parameter node holding the value of the parameter.
	 */
	private static final String	PARAMETER_VALUE		= "value";
	
	/**
	 * This enumeration contains all supported platforms.
	 * 
	 * @author Andrei Olaru
	 */
	public static enum Platform {
		/**
		 * The current machine runs an OS that contains a standard Java VM.
		 */
		PC,
		
		/**
		 * The current machine runs an OS that uses the Dalvik VM.
		 */
		ANDROID,
	}
	
	/**
	 * @return the current platform, as an instance of {@link Platform}.
	 */
	public static Platform getPlatform()
	{
		if(System.getProperty("java.vm.name").equals("Dalvik"))
		{
			
			return Platform.ANDROID;
		}
		
		return Platform.ANDROID;
	}
	
	/**
	 * FIXME: should be split into WS registration and WS invocation.
	 * 
	 * @return <code>true</code> if the current platform supports web service registration and invocation.
	 */
	public static boolean platformSupportsWebServices()
	{
		return false;
	}
	
	/**
	 * @return the type of log (on of {@link LoggerType}) appropriate for the current platform.
	 */
	/*
	public static LogWrapper.LoggerType platformLogType()
	{
		switch(getPlatform())
		{
		case PC:
//			return LoggerType.CONSOLE;
			return LogWrapper.LoggerType.LOG4J;
		case ANDROID:
			return LogWrapper.LoggerType.JAVA;
		}
		return null;
	}
	*/
	/**
	 * @return the name of the class implementing {@link JadeInterface} that is appropriate to use for the current
	 *         platform.
	 */
	public static String jadeInterfaceClass()
	{
		switch(getPlatform())
		{
		case PC:
			return "tatami.pc.util.jade.PCJadeInterface";
		case ANDROID:
			return "org.interfaces.AndroidJadeInterface";
		}
		return null;
	}
	
	/**
	 * Checks if the specified class exists.
	 * 
	 * @param className
	 *            - the fully qualified name of the class.
	 * @return <code>true</code> if the class exists, <code>false</code> otherwise.
	 */
	public static boolean classExists(String className)
	{
		try
		{
			Class.forName(className);
			return true;
		} catch(ClassNotFoundException e)
		{
			return false;
		}
	}
	

	
	/**
	 * Creates a new instance of a class that is not known at compile-time.
	 * 
	 * @param loadingClass
	 *            - an instance created with the class loader to use to create the new instance. Can be
	 *            <code>new Object()</code>.
	 * @param className
	 *            - the name of the class to instantiate.
	 * @param constructorArguments
	 *            - an object array specifying the arguments to pass to the constructor of the new instance. The types
	 *            of the objects in this array will be used to identify the constructor of the new instance.
	 * @return the newly created instance. If the creation fails, an exception will be surely thrown.
	 * @throws SecurityException
	 *             -
	 * @throws NoSuchMethodException
	 *             -
	 * @throws ClassNotFoundException
	 *             -
	 * @throws IllegalArgumentException
	 *             -
	 * @throws InstantiationException
	 *             -
	 * @throws IllegalAccessException
	 *             -
	 * @throws InvocationTargetException
	 *             -
	 */
	public static Object loadClassInstance(Object loadingClass, String className, Object... constructorArguments)
			throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException,
			InstantiationException, IllegalAccessException, InvocationTargetException
	{
		ClassLoader cl = null;
		cl = new ClassLoader(loadingClass.getClass().getClassLoader()) {
			// nothing to extend
		};
		Class<?>[] argumentTypes = new Class<?>[constructorArguments.length];
		int i = 0;
		for(Object obj : constructorArguments)
			argumentTypes[i++] = obj.getClass();
		Constructor<?> constructor = cl.loadClass(className).getConstructor(argumentTypes);
		Object ret = constructor.newInstance(constructorArguments);
		return ret;
	}
	
	/**
	 * @return the class for the Simulation Manager GUI.
	 */
	public static String getSimulationGuiClass()
	{
		String platformName = getPlatform().toString();
		switch(getPlatform())
		{
		case PC:
			return "tatami." + platformName.toLowerCase() + ".agent.visualization." + platformName.toUpperCase()
					+ "SimulationGui";
		default:
			break;
		}
		return null;
	}
	
	/**
	 * Converts the arguments into a {@link Vector} containing all arguments passed to the method.
	 * 
	 * @param arguments
	 *            the arguments to assemble into the vector.
	 * @return the vector containing all arguments.
	 */
	public static Vector<Object> toVector(Object... arguments)
	{
		return new Vector<Object>(Arrays.asList(arguments));
	}
	
	/**
	 * Method to print an exception as a string.
	 * 
	 * @param e
	 *            - the exception.
	 * @return a {@link String} containing all details of the exception.
	 */
	public static String printException(Exception e)
	{
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
	/**
	 * Method to simplify the access to a parameter of an agent. Having the {@link XMLNode} instance associated with the
	 * agent, the method retrieves the value associated with the first occurrence of the desired parameter name.
	 * 
	 * @param XMLnode
	 *            - the node containing the configuration information for the agent.
	 * @param parameterName
	 *            - the name of the searched parameter.
	 * @return the value associated with the searched name.
	 */
	public static String getParameterValue(XMLNode XMLnode, String parameterName)
	{
		return XMLnode.getAttributeOfFirstNodeWithValue(PARAMETER_NODE_NAME, PARAMETER_NAME, parameterName,
				PARAMETER_VALUE);
	}
	
	/**
	 * Performs system exit, depending on platform.
	 * 
	 * @param exitCode
	 *            - the exit code.
	 */
	public static void systemExit(int exitCode)
	{
		System.exit(0);
	}
	
}
