/*
 *     Copyright (C) 2016  Nodin Chan
 *     
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *     
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *     
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see {http://www.gnu.org/licenses/}.
 */

package com.nodinchan.dev.tools.loading;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Loader for JAR Files
 * 
 * @author Nodin
 */
public final class Loader<T> {
	
	public static final Predicate<File> VALID_FILE = (file) -> Objects.nonNull(file) && file.getName().endsWith(".jar");
	
	private static final Logger LOGGER = Logger.getLogger("Loader");
	private static final String PROPERTIES = "module.properties";
	
	private final Settings<T> settings;
	
	private Loader(Settings<T> settings) {
		this.settings = settings;
	}
	
	/**
	 * Gets applicable files based on the additional {@link Predicate}
	 * 
	 * @param directory The directory to look in
	 * @param filter {@link Predicate} for filtering files applicable
	 * 
	 * @return An array of applicable files
	 */
	private static File[] getFiles(File directory, Predicate<File> filter) {
		return directory.listFiles((file) -> VALID_FILE.and(filter).test(file));
	}
	
	/**
	 * Gets the {@link Properties} file from the JAR file
	 * 
	 * @param jar The JAR file to look in
	 * 
	 * @return The {@link Properties} object if found
	 * 
	 * @throws IOException Thrown when the file is not found
	 */
	private static Properties getProperties(JarFile jar) throws IOException {
		JarEntry entry = jar.getJarEntry(PROPERTIES);
		
		if (Objects.isNull(entry))
			throw new FileNotFoundException("The file \"" + PROPERTIES + "\" was not found");
		
		Properties properties = new Properties();
		
		properties.load(jar.getInputStream(entry));
		
		return properties;
	}
	
	/**
	 * Loads the JAR file
	 * 
	 * @param type The class type of the result
	 * @param file The file to be loaded
	 * @param parameters The parameters of the constructor
	 * @param init Function to initialise after instantiation
	 * 
	 * @return The object instance that was created
	 */
	private static <T> T load(Class<T> type, File file, Object[] parameters, Initialiser<T> init) {
		if (!VALID_FILE.test(file))
			throw new IllegalArgumentException("File is invalid");
		
		T module = null;
		
		try (JarFile jar = new JarFile(file)) {
			Properties properties = getProperties(jar);
			
			Class<?> loadedClass = Class.forName(properties.getProperty("main-class", ""), true, URLClassLoader.newInstance(new URL[] { file.toURI().toURL() }));
			Class<? extends T> moduleClass = loadedClass.asSubclass(type);
			Constructor<? extends T> moduleConstructor = moduleClass.getConstructor(toParameterTypes(parameters));
			
			module = init.initialise(moduleConstructor.newInstance(parameters), properties);
			
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.WARNING, "The main class of " + file.getName() + " cannot be found", e);
			
		} catch (ReflectiveOperationException | SecurityException e) {
			LOGGER.log(Level.WARNING, "The main class of " + file.getName() + " cannot be instantiated", e);
			
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "The file \"" + file.getName() + "\" failed to load", e);
		}
		
		return module;
	}
	
	/**
	 * Loads the JAR file
	 * 
	 * @param file The file to be loaded
	 * @param settings The settings for the operation
	 * 
	 * @return An {@link Optional} containing the object if successful, otherwise empty
	 */
	public static <T> Optional<T> loadFile(File file, Settings<T> settings) {
		if (Objects.isNull(settings))
			throw new IllegalArgumentException(new NullPointerException("Settings cannot be null"));
		
		return Optional.ofNullable(load(settings.getType(), file, settings.getParameters(), settings.getInitialiser()));
	}
	
	/**
	 * Loads the JAR file
	 * 
	 * @param type The class type of the result
	 * @param file The file to be loaded
	 * @param parameters The parameters of the constructor
	 * 
	 * @return An {@link Optional} containing the object if successful, otherwise empty
	 */
	public static <T> Optional<T> loadFile(Class<T> type, File file, Object... parameters) {
		if (Objects.isNull(type))
			throw new IllegalArgumentException(new NullPointerException("The class type cannot be null"));
		
		return Optional.ofNullable(load(type, file, parameters, (module, properties) -> module));
	}
	
	/**
	 * Loads the JAR files
	 * 
	 * @param files The files to be loaded
	 * @param settings The settings for the operation
	 * 
	 * @return A {@link Set} of the object instances that were created
	 */
	public static <T> Set<T> loadFiles(File[] files, Settings<T> settings) {
		if (Objects.isNull(files))
			throw new IllegalArgumentException(new NullPointerException("Files cannot be null"));
		
		return Stream.of(files).map((file) -> loadFile(file, settings)).filter((module) -> module.isPresent()).map((module) -> module.get()).collect(Collectors.toSet());
	}
	
	/**
	 * Loads the JAR files in the directory
	 * 
	 * @param directory The directory containing the files to be loaded
	 * @param settings The settings for the operation
	 * 
	 * @return A {@link Set} of the object instances that were created
	 */
	public static <T> Set<T> loadFiles(File directory, Settings<T> settings) {
		if (Objects.isNull(directory))
			throw new IllegalArgumentException(new NullPointerException("The directory cannot be null"));
		
		return loadFiles(getFiles(directory, settings.getFilter()), settings);
	}
	
	/**
	 * Loads the JAR file
	 * 
	 * @param file The file to be loaded
	 * 
	 * @return The object instance that was created
	 */
	public Optional<T> loadFile(File file) {
		return loadFile(file, settings);
	}
	
	/**
	 * Loads the JAR files
	 * 
	 * @param files The files to be loaded
	 * 
	 * @return A {@link Set} of the object instances that were created
	 */
	public Set<T> loadFiles(File... files) {
		return loadFiles(files, settings);
	}
	
	/**
	 * Loads the JAR files
	 * 
	 * @param directory The directory containing the files to be loaded
	 * 
	 * @return A {@link Set} of the object instances that were created
	 */
	public Set<T> loadFiles(File directory) {
		return loadFiles(directory, settings);
	}
	
	/**
	 * Creates a new instance of the {@link Loader}
	 * 
	 * @param settings The settings for the instance
	 * 
	 * @return {@link Loader}
	 */
	public static <T> Loader<T> newInstance(Settings<T> settings) {
		return new Loader<T>(settings);
	}
	
	/**
	 * Gets the class types of the specified parameters
	 * 
	 * @param parameters The parameters
	 * 
	 * @return An array of class types
	 */
	private static Class<?>[] toParameterTypes(Object... parameters) {
		return Stream.of(parameters).map((object) -> object.getClass()).toArray((size) -> new Class<?>[size]);
	}
}