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
import java.util.Collections;
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
 * Loader - A simple library for Java class loading
 * 
 * @author Nodin
 */
public final class Loader<M> {
	
	private static final Logger LOGGER = Logger.getLogger("Loader");
	private static final String PROPERTIES = "module.properties";
	
	private final Settings<M> settings;
	
	private Loader(Settings<M> settings) {
		this.settings = settings;
	}
	
	/**
	 * Checks if the {@link File} is applicable
	 * 
	 * @param file The {@link File} to inspect
	 * 
	 * @return True if the file is applicable, otherwise false
	 */
	private static boolean accept(File file) {
		return Objects.nonNull(file) && file.getName().endsWith(".jar");
	}
	
	/**
	 * Gets the {@link Properties} file from the JAR file
	 * 
	 * @param jar The JAR file to look in
	 * 
	 * @return A loaded {@link Properties} object if found
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
	 * Loads the JAR file with the given settings
	 * 
	 * @param file The {@link File} to be loaded
	 * @param settings The settings for the operation
	 * 
	 * @return An {@link Optional} containing a {@link Module} instance if successful
	 */
	public static <M> Optional<Module<M>> load(File file, Settings<M> settings) {
		if (Objects.isNull(settings))
			throw new IllegalArgumentException(new NullPointerException("Settings cannot be null"));
		
		if (!accept(file))
			throw new IllegalArgumentException("The file " + file + " is invalid");
		
		Module<M> module = null;
		
		try (JarFile jar = new JarFile(file)) {
			Properties properties = getProperties(jar);
			
			ClassLoader loader = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() });
			
			Class<?> loadedClass = Class.forName(properties.getProperty("main-class", ""), true, loader);
			Class<? extends M> moduleClass = loadedClass.asSubclass(settings.getType());
			Constructor<? extends M> moduleConstructor = moduleClass.getConstructor(settings.getSignature());
			
			module = new Module<M>(moduleConstructor.newInstance(settings.getArguments(properties)), loader, file);
			
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.WARNING, "The main class of " + file.getName() + " cannot be found", e);
			
		} catch (ReflectiveOperationException | SecurityException e) {
			LOGGER.log(Level.WARNING, "The main class of " + file.getName() + " cannot be instantiated", e);
			
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "The file \"" + file.getName() + "\" failed to load", e);
		}
		
		return Optional.ofNullable(module);
	}
	
	/**
	 * Loads the JAR files with the given settings
	 * 
	 * @param files The {@link File}s to be loaded
	 * @param settings The settings for the operation
	 * 
	 * @return A {@link Set} of {@link Module} instances
	 */
	public static <M> Set<Module<M>> load(File[] files, Settings<M> settings) {
		if (Objects.isNull(files))
			return Collections.emptySet();
		
		return Stream.of(files).map((file) -> load(file, settings)).filter((optional) -> optional.isPresent()).map((optional) -> optional.get()).collect(Collectors.toSet());
	}
	
	/**
	 * Loads the JAR files in the directory with the given settings
	 * 
	 * @param directory The directory to look in
	 * @param filter A {@link Predicate} for additional filtering
	 * @param settings The settings for the operation
	 * 
	 * @return A {@link Set} of {@link Module} instances
	 */
	public static <M> Set<Module<M>> load(File directory, Predicate<File> filter, Settings<M> settings) {
		if (Objects.isNull(directory) || !directory.exists())
			return Collections.emptySet();
		
		return load(directory.listFiles((file) -> accept(file) && filter.test(file)), settings);
	}
	
	/**
	 * Loads the JAR file
	 * 
	 * @param file The {@link File} to be loaded
	 * 
	 * @return An {@link Optional} containing a {@link Module} instance if successful
	 */
	public Optional<Module<M>> load(File file) {
		return load(file, settings);
	}
	
	/**
	 * Loads the JAR files
	 * 
	 * @param files The {@link File}s to be loaded
	 * 
	 * @return A {@link Set} of {@link Module} instances
	 */
	public Set<Module<M>> load(File[] files) {
		return load(files, settings);
	}
	
	/**
	 * Loads the JAR files in the directory
	 * 
	 * @param directory The directory to look in
	 * @param filter A {@link Predicate} for additional filtering
	 * 
	 * @return A {@link Set} of {@link Module} instances
	 */
	public Set<Module<M>> load(File directory, Predicate<File> filter) {
		return load(directory, filter, settings);
	}
	
	/**
	 * Creates a {@link Loader} instance with the given settings
	 * 
	 * @param settings The settings of the {@link Loader}
	 * 
	 * @return A {@link Loader} instance
	 */
	public static <M> Loader<M> newInstance(Settings<M> settings) {
		if (Objects.isNull(settings))
			throw new IllegalArgumentException(new NullPointerException("Settings cannot be null"));
		
		return new Loader<M>(settings);
	}
}