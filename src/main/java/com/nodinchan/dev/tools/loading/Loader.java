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
import java.lang.reflect.InvocationTargetException;
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
	
	private final Class<T> type;
	private final InitFunction<T> init;
	
	private Loader(Class<T> type, InitFunction<T> init) {
		this.type = type;
		this.init = (Objects.nonNull(init)) ? init : (module, properties) -> module;
	}
	
	/**
	 * Creates a new instance of the {@link Loader}
	 * 
	 * @param type The class type of the results
	 * @param init Function to initialise after instantiation
	 * 
	 * @return {@link Loader}
	 */
	public static <T> Loader<T> newInstance(Class<T> type, InitFunction<T> init) {
		return new Loader<T>(type, init);
	}
	
	/**
	 * Creates a new instance of the {@link Loader}
	 * 
	 * @param type The class type of the results
	 * 
	 * @return {@link Loader}
	 */
	public static <T> Loader<T> newInstance(Class<T> type) {
		return new Loader<T>(type, (module, properties) -> module);
	}
	
	/**
	 * Loads the JAR file
	 * 
	 * @param type The class type of the result
	 * @param file The file to be loaded
	 * @param init Function to initialise after instantiation
	 * 
	 * @return The object instance that was created
	 */
	public static <T> Optional<T> load(Class<T> type, File file, InitFunction<T> init) {
		if (Objects.isNull(type))
			throw new IllegalArgumentException(new NullPointerException("Class Type cannot be null"));
		
		if (VALID_FILE.test(file))
			throw new IllegalArgumentException("File is invalid");
		
		if (Objects.isNull(init))
			init = (module, properties) -> module;
		
		T module = null;
		
		try (JarFile jar = new JarFile(file)) {
			JarEntry entry = jar.getJarEntry(PROPERTIES);
			
			if (Objects.isNull(entry))
				throw new FileNotFoundException("The file \"" + PROPERTIES + "\" was not found");
			
			Properties properties = new Properties();
			
			properties.load(jar.getInputStream(entry));
			
			Class<?> loadedClass = Class.forName(properties.getProperty("main-class", ""), true, URLClassLoader.newInstance(new URL[] { file.toURI().toURL() }));
			Class<? extends T> moduleClass = loadedClass.asSubclass(type);
			Constructor<? extends T> moduleConstructor = moduleClass.getConstructor();
			
			module = init.initialise(moduleConstructor.newInstance(), properties);
			
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.WARNING, "The main class of " + file.getName() + " cannot be found", e);
			
		} catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			LOGGER.log(Level.WARNING, "The main class of " + file.getName() + " cannot be instantiated", e);
			
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "The file \"" + file.getName() + "\" failed to load", e);
		}
		
		return Optional.ofNullable(module);
	}
	
	/**
	 * Loads the JAR files
	 * 
	 * @param type The class type of the result
	 * @param files The files to be loaded
	 * @param init Function to initialise after instantiation
	 * 
	 * @return A {@link Set} of the object instances that were created
	 */
	public static <T> Set<T> load(Class<T> type, File[] files, InitFunction<T> init) {
		if (Objects.isNull(files))
			throw new IllegalArgumentException(new NullPointerException("Files cannot be null"));
		
		return Stream.of(files).map((file) -> load(type, file, init)).filter((module) -> module.isPresent()).map((module) -> module.get()).collect(Collectors.toSet());
	}
	
	/**
	 * Loads the JAR files
	 * 
	 * @param type The class type of the result
	 * @param directory The directory containing the files to be loaded
	 * @param filter {@link Predicate} for filtering files applicable
	 * @param init Function to initialise after instantiation
	 * 
	 * @return A {@link Set} of the object instances that were created
	 */
	public static <T> Set<T> load(Class<T> type, File directory, Predicate<File> filter, InitFunction<T> init) {
		if (Objects.isNull(directory))
			throw new IllegalArgumentException(new NullPointerException("Directory cannot be null"));
		
		return load(type, directory.listFiles((Objects.nonNull(filter)) ? (file) -> VALID_FILE.and(filter).test(file) : (file) -> VALID_FILE.test(file)), init);
	}
	
	/**
	 * Loads the JAR file
	 * 
	 * @param type The class type of the result
	 * @param file The file to be loaded
	 * 
	 * @return The object instance that was created
	 */
	public static <T> Optional<T> load(Class<T> type, File file) {
		return load(type, file, (module, properties) -> module);
	}
	
	/**
	 * Loads the JAR files
	 * 
	 * @param type The class type of the result
	 * @param files The files to be loaded
	 * 
	 * @return A {@link Set} of the object instances that were created
	 */
	public static <T> Set<T> load(Class<T> type, File[] files) {
		return load(type, files, (module, properties) -> module);
	}
	
	/**
	 * Loads the JAR files
	 * 
	 * @param type The class type of the result
	 * @param directory The directory containing the files to be loaded
	 * @param filter {@link Predicate} for filtering files applicable
	 * 
	 * @return A {@link Set} of the object instances that were created
	 */
	public static <T> Set<T> load(Class<T> type, File directory, Predicate<File> filter) {
		return load(type, directory, filter, (module, properties) -> module);
	}
	
	/**
	 * Loads the JAR file
	 * 
	 * @param file The file to be loaded
	 * 
	 * @return The object instance that was created
	 */
	public Optional<T> load(File file) {
		return load(type, file, init);
	}
	
	/**
	 * Loads the JAR files
	 * 
	 * @param files The files to be loaded
	 * 
	 * @return A {@link Set} of the object instances that were created
	 */
	public Set<T> load(File... files) {
		return load(type, files, init);
	}
	
	/**
	 * Loads the JAR files
	 * 
	 * @param directory The directory containing the files to be loaded
	 * @param filter {@link Predicate} for filtering files applicable
	 * 
	 * @return A {@link Set} of the object instances that were created
	 */
	public Set<T> load(File directory, Predicate<File> filter) {
		return load(type, directory, filter, init);
	}
	
	/**
	 * Function to initialise object instances after creation
	 * 
	 * @author Nodin
	 */
	@FunctionalInterface
	public static interface InitFunction<T> {
		
		/**
		 * Initialises the object instance
		 * 
		 * @param module The object instance to initialise
		 * @param properties The properties file contained in the JAR file
		 * 
		 * @return The initialised object instance
		 */
		public T initialise(T module, Properties properties);
	}
}