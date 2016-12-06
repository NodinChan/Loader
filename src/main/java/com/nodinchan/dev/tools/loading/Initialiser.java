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

import java.util.Properties;

/**
 * {@link FunctionalInterface} for the initialisation of loaded objects
 * 
 * @author Nodin
 */
@FunctionalInterface
public interface Initialiser<T> {
	
	/**
	 * Initialises the object instance
	 * 
	 * @param module The object to initialise
	 * @param properties The {@link Properties} file contained in the JAR file
	 * 
	 * @return The initialised object instance
	 */
	public T initialise(T module, Properties properties);
}