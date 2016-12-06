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
import java.util.function.Predicate;

/**
 * {@link FunctionalInterface} containing settings for loading operations
 * 
 * @author Nodin
 */
@FunctionalInterface
public interface Settings<T> {
	
	/**
	 * Gets a {@link Predicate} for additional filtering of files
	 * 
	 * @return {@link Predicate}
	 */
	public default Predicate<File> getFilter() {
		return (file) -> true;
	}
	
	/**
	 * Gets the {@link Initialiser} for the initialisation of loaded objects
	 * 
	 * @return {@link Initialiser}
	 */
	public default Initialiser<T> getInitialiser() {
		return (module, properties) -> module;
	}
	
	/**
	 * Gets the parameters of the constructor of the class to be loaded
	 * 
	 * @return An array of objects
	 */
	public default Object[] getParameters() {
		return new Object[0];
	}
	
	/**
	 * Gets the expected class type of the result
	 * 
	 * @return The class type of the loaded objects
	 */
	public Class<T> getType();
}