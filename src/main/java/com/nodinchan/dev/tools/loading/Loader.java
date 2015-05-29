/*
 *     Copyright (C) 2015  Nodin Chan
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
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Loader<T> {
	
	private static final File DIRECTORY = new File("loadables");
	private static final Filter FILTER = new Filter();
	private static final Logger LOG = Logger.getLogger("Loader");
	private static final String PROPERTIES = "loadable.properties";
	
	private final Class<T> clazz;
	
	private final File directory;
	private final Filter filter;
	private final Logger log;
	private final String properties;
	
	private Loader(Class<T> clazz, File directory, String properties, Filter filter, Logger log) {
		this.clazz = clazz;
		this.directory = directory;
		this.filter = filter;
		this.log = log;
		this.properties = properties;
	}
	
	public static <T> LoaderBuilder<T> builder(Class<T> clazz) {
		return new LoaderBuilder<T>(clazz);
	}
	
	public List<T> load() {
		List<T> loadables = new ArrayList<>();
		
		for (File file : directory.listFiles(filter)) {
			JarFile jar = null;
			
			try {
				jar = new JarFile(file);
				JarEntry entry = jar.getJarEntry(properties);
				
				if (entry == null)
					throw new IllegalStateException("The file " + properties + " was not found");
				
				ClassLoader loader = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() });
				
				Properties description = new Properties();
				description.load(jar.getInputStream(entry));
				
				String main = description.getProperty("main-class", "");
				
				Class<?> loadedClass = Class.forName(main, true, loader);
				Class<? extends T> loadableClass = loadedClass.asSubclass(clazz);
				Constructor<? extends T> loadableConstructor = loadableClass.getConstructor();
				
				loadables.add(loadableConstructor.newInstance());
				
			} catch (Exception e) {
				log.log(Level.WARNING, "The file " + file.getName() + " failed to load");
				e.printStackTrace();
				
			} finally {
				if (jar != null)
					try { jar.close(); } catch (Exception e) { e.printStackTrace(); }
			}
		}
		
		return loadables;
	}
	
	public T load(File file) {
		if (!filter.accept(file))
			throw new IllegalArgumentException("File must be a JAR file");
		
		T loadable = null;
		
		JarFile jar = null;
		
		try {
			jar = new JarFile(file);
			JarEntry entry = jar.getJarEntry(properties);
			
			if (entry == null)
				throw new IllegalStateException("The file " + properties + " was not found");
			
			ClassLoader loader = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() });
			
			Properties description = new Properties();
			description.load(jar.getInputStream(entry));
			
			String main = description.getProperty("main-class", "");
			
			Class<?> loadedClass = Class.forName(main, true, loader);
			Class<? extends T> loadableClass = loadedClass.asSubclass(clazz);
			Constructor<? extends T> loadableConstructor = loadableClass.getConstructor();
			
			loadable = loadableConstructor.newInstance();
			
		} catch (Exception e) {
			log.log(Level.WARNING, "The file " + file.getName() + " failed to load");
			e.printStackTrace();
			
		} finally {
			if (jar != null)
				try { jar.close(); } catch (Exception e) { e.printStackTrace(); }
		}
		
		return loadable;
	}
	
	public static <T> T load(Class<T> clazz, File file, String properties, Logger log) {
		if (clazz == null)
			throw new IllegalArgumentException(new NullPointerException("Class cannot be null"));
		
		if (!FILTER.accept(file))
			throw new IllegalArgumentException("File must be a JAR file");
		
		if (properties == null)
			properties = PROPERTIES;
		
		if (log == null)
			log = LOG;
		
		T loadable = null;
		
		JarFile jar = null;
		
		try {
			jar = new JarFile(file);
			JarEntry entry = jar.getJarEntry(properties);
			
			if (entry == null)
				throw new IllegalStateException("The file " + properties + " was not found");
			
			ClassLoader loader = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() });
			
			Properties description = new Properties();
			description.load(jar.getInputStream(entry));
			
			String main = description.getProperty("main-class", "");
			
			Class<?> loadedClass = Class.forName(main, true, loader);
			Class<? extends T> loadableClass = loadedClass.asSubclass(clazz);
			Constructor<? extends T> loadableConstructor = loadableClass.getConstructor();
			
			loadable = loadableConstructor.newInstance();
			
		} catch (Exception e) {
			log.log(Level.WARNING, "The file " + file.getName() + " failed to load");
			e.printStackTrace();
			
		} finally {
			if (jar != null)
				try { jar.close(); } catch (Exception e) { e.printStackTrace(); }
		}
		
		return loadable;
	}
	
	public static <T> List<T> load(Class<T> clazz, File directory, String properties, Filter filter, Logger log) {
		if (clazz == null)
			throw new IllegalArgumentException(new NullPointerException("Class cannot be null"));
		
		if (directory == null)
			directory = DIRECTORY;
		
		if (properties == null)
			properties = PROPERTIES;
		
		directory.mkdirs();
		
		if (filter == null)
			filter = FILTER;
		
		if (log == null)
			log = LOG;
		
		List<T> loadables = new ArrayList<>();
		
		for (File file : directory.listFiles(filter)) {
			JarFile jar = null;
			
			try {
				jar = new JarFile(file);
				JarEntry entry = jar.getJarEntry(properties);
				
				if (entry == null)
					throw new IllegalStateException("The file " + properties + " was not found");
				
				ClassLoader loader = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() });
				
				Properties description = new Properties();
				description.load(jar.getInputStream(entry));
				
				String main = description.getProperty("main-class", "");
				
				Class<?> loadedClass = Class.forName(main, true, loader);
				Class<? extends T> loadableClass = loadedClass.asSubclass(clazz);
				Constructor<? extends T> loadableConstructor = loadableClass.getConstructor();
				
				loadables.add(loadableConstructor.newInstance());
				
			} catch (Exception e) {
				log.log(Level.WARNING, "The file " + file.getName() + " failed to load");
				e.printStackTrace();
				
			} finally {
				if (jar != null)
					try { jar.close(); } catch (Exception e) { e.printStackTrace(); }
			}
		}
		
		return loadables;
	}
	
	public static final class LoaderBuilder<T> {
		
		private final Class<T> clazz;
		
		private File directory = DIRECTORY;
		private Filter filter = FILTER;
		private Logger log = LOG;
		private String properties = PROPERTIES;
		
		private LoaderBuilder(Class<T> clazz) {
			if (clazz == null)
				throw new IllegalArgumentException(new NullPointerException("Class cannot be null"));
			
			this.clazz = clazz;
		}
		
		public Loader<T> build() {
			return new Loader<T>(clazz, directory, properties, filter, log);
		}
		
		public LoaderBuilder<T> setDirectory(File directory) {
			(this.directory = (directory == null) ? DIRECTORY : directory).mkdirs();
			return this;
		}
		
		public LoaderBuilder<T> setFilter(Filter filter) {
			this.filter = (filter == null) ? FILTER : filter;
			return this;
		}
		
		public LoaderBuilder<T> setLogger(Logger log) {
			this.log = (log == null) ? LOG : log;
			return this;
		}
		
		public LoaderBuilder<T> setPropertiesFile(String properties) {
			this.properties = (properties == null || properties.isEmpty()) ? PROPERTIES : properties;
			return this;
		}
	}
}