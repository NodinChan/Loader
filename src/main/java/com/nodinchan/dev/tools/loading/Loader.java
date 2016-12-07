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
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Loader<T> {
	
	public static final Predicate<File> JAR_FILE = (file) -> Objects.nonNull(file) && file.getName().endsWith(".jar");
	
	private static final File DIRECTORY = new File("loadables");
	private static final FileFilter FILTER = (file) -> JAR_FILE.test(file);
	private static final Logger LOG = Logger.getLogger("Loader");
	private static final String PROPERTIES = "loadable.properties";
	
	private final Class<T> type;
	
	private final File directory;
	private final FileFilter filter;
	
	private final String properties;
	
	private final Logger log;

	private Loader(Class<T> type, File directory, FileFilter filter, String properties, Logger log) {
		this.type = type;
		this.directory = directory;
		this.filter = filter;
		this.properties = properties;
		this.log = log;
	}
	
	public static <T> LoaderBuilder<T> builder(Class<T> type) {
		if (type == null)
			throw new IllegalArgumentException(new NullPointerException("Class 'type' cannot be null"));
		
		return new LoaderBuilder<T>(type);
	}
	
	public Optional<T> load(File file) {
		return uncheckedLoad(type, file, properties, log);
	}
	
	public List<T> load() {
		return toLoadables(uncheckedLoad(type, directory, filter, properties, log));
	}
	
	public static <T> Optional<T> load(Class<T> type, File file, String properties, Logger log) {
		if (Objects.isNull(type))
			throw new IllegalArgumentException(new NullPointerException("Class 'type' cannot be null"));
		
		if (JAR_FILE.negate().test(file))
			throw new IllegalArgumentException("File 'file' is not a valid JAR File");
		
		if (Objects.isNull(properties))
			properties = PROPERTIES;
		
		if (Objects.isNull(log))
			log = LOG;
		
		return uncheckedLoad(type, file, properties, log);
	}
	
	public static <T> List<T> load(Class<T> type, File directory, Predicate<File> predicate, String properties, Logger log) {
		if (Objects.isNull(type))
			throw new IllegalArgumentException(new NullPointerException("Class 'type' cannot be null"));
		
		if (Objects.isNull(directory))
			directory = DIRECTORY;
		
		directory.mkdirs();
		
		FileFilter filter = (Objects.isNull(predicate)) ? FILTER : ((file) -> JAR_FILE.and(predicate).test(file));
		
		if (Objects.isNull(properties))
			properties = PROPERTIES;
		
		if (Objects.isNull(log))
			log = LOG;
		
		return toLoadables(uncheckedLoad(type, directory, filter, properties, log));
	}
	
	private static <T> Optional<T> uncheckedLoad(Class<T> type, File file, String properties, Logger log) {
		T loadable = null;
		
		try (JarFile jar = new JarFile(file)) {
			JarEntry entry = jar.getJarEntry(properties);
			
			if (Objects.isNull(entry))
				throw new IllegalStateException("The file " + properties + " was not found");
			
			ClassLoader loader = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() });
			
			Properties description = new Properties();
			description.load(jar.getInputStream(entry));
			
			String main = description.getProperty("main-class", "");
			
			Class<?> loadedClass = Class.forName(main, true, loader);
			Class<? extends T> loadableClass = loadedClass.asSubclass(type);
			Constructor<? extends T> loadableConstructor = loadableClass.getConstructor();
			
			loadable = loadableConstructor.newInstance();
			
		} catch (Exception e) {
			log.log(Level.WARNING, "The file " + file.getName() + " failed to load");
			e.printStackTrace();
		}
		
		return Optional.ofNullable(loadable);
	}
	
	private static <T> Stream<Optional<T>> uncheckedLoad(Class<T> type, File directory, FileFilter filter, String properties, Logger log) {
		return Arrays.stream(directory.listFiles(filter)).map((file) -> uncheckedLoad(type, file, properties, log));
	}
	
	private static <T> List<T> toLoadables(Stream<Optional<T>> loadables) {
		return loadables.filter((optional) -> optional.isPresent()).map((optional) -> optional.get()).collect(Collectors.toList());
	}
	
	public static final class LoaderBuilder<T> {
		
		private final Class<T> type;
		
		private File directory;
		private Predicate<File> predicate;
		
		private String properties;
		
		private Logger log;
		
		private LoaderBuilder(Class<T> type) {
			this.type = type;
			this.directory = DIRECTORY;
			this.predicate = JAR_FILE;
			this.properties = PROPERTIES;
			this.log = LOG;
		}
		
		public Loader<T> build() {
			return new Loader<T>(type, directory, predicate::test, properties, log);
		}
		
		public File getDirectory() {
			return directory;
		}
		
		public Predicate<File> getFilterSettings() {
			return predicate;
		}
		
		public Logger getLogger() {
			return log;
		}
		
		public String getPropertiesFile() {
			return properties;
		}
		
		public LoaderBuilder<T> setDirectory(File directory) {
			this.directory = (Objects.isNull(directory)) ? DIRECTORY : directory;
			return this;
		}
		
		public LoaderBuilder<T> setFilterSettings(Predicate<File> predicate) {
			this.predicate = (Objects.isNull(predicate)) ? JAR_FILE : JAR_FILE.and(predicate);
			return this;
		}
		
		public LoaderBuilder<T> setLogger(Logger log) {
			this.log = (Objects.isNull(log)) ? LOG : log;
			return this;
		}
		
		public LoaderBuilder<T> setPropertiesFile(String properties) {
			this.properties = (Objects.isNull(properties)) ? PROPERTIES : properties;
			return this;
		}
	}
}