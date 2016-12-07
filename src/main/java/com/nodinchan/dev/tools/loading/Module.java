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
import java.io.InputStream;

/**
 * Module - Container for loaded objects
 * 
 * @author Nodin
 *
 * @param <M>
 */
public final class Module<M> {
	
	private final M module;
	
	private final ClassLoader loader;
	
	private final File file;
	
	protected Module(M module, ClassLoader loader, File file) {
		this.module = module;
		this.loader = loader;
		this.file = file;
	}
	
	/**
	 * Gets the object that was loaded
	 * 
	 * @return The object that was loaded
	 */
	public M get() {
		return module;
	}
	
	/**
	 * Gets the {@link ClassLoader} which holds the loaded object
	 * 
	 * @return The {@link ClassLoader} holding the loaded object
	 */
	public ClassLoader getClassLoader() {
		return loader;
	}
	
	/**
	 * Gets the {@link File} that was loaded
	 * 
	 * @return The {@link File} that was loaded
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Gets an embedded resource from the JAR
	 * 
	 * @param name The file name of the resource
	 * 
	 * @return An {@link InputStream} of the resource if found, otherwise null
	 */
	public InputStream getResource(String name) {
		return loader.getResourceAsStream(name);
	}
}