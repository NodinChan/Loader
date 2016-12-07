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

import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;

/**
 * Settings - Specification of the class type, constructor, and arguments to be used
 * 
 * @author Nodin
 *
 * @param <M>
 */
public final class Settings<M> {
	
	protected static final Class<?>[] NO_PARAMETERS = new Class<?>[0];
	protected static final Object[] NO_ARGUMENTS = new Object[0];
	
	private final Class<M> type;
	
	private final Class<?>[] signature;
	private final Function<Properties, Object[]> arguments;
	
	private Settings(Class<M> type, Class<?>[] signature, Function<Properties, Object[]> parameters) {
		this.type = type;
		this.signature = (Objects.nonNull(signature)) ? signature : NO_PARAMETERS;
		this.arguments = (Objects.nonNull(parameters)) ? parameters : (properties) -> NO_ARGUMENTS;
	}
	
	/**
	 * Gets the arguments to be used in instantiation
	 * 
	 * @param properties The {@link Properties} file in the JAR file
	 * 
	 * @return An array of {@link Object}s as arguments
	 */
	public Object[] getArguments(Properties properties) {
		return arguments.apply(properties);
	}
	
	/**
	 * Gets the parameter types of the constructor
	 * 
	 * @return An array of {@link Class}es as parameter types
	 */
	public Class<?>[] getSignature() {
		return signature;
	}
	
	/**
	 * Gets the class type of the loaded object
	 * 
	 * @return The class type of the loaded object
	 */
	public Class<M> getType() {
		return type;
	}
	
	/**
	 * Creates a {@link Settings} object with the specified type, signature, and arguments
	 * 
	 * @param type The class type of the loaded object
	 * @param signature The parameter types of the constructor
	 * @param arguments The arguments to be used in instantiation
	 * 
	 * @return A {@link Settings} object with the specified type, signature, and arguments
	 */
	public static <M> Settings<M> of(Class<M> type, Class<?>[] signature, Function<Properties, Object[]> arguments) {
		if (Objects.isNull(type))
			throw new IllegalArgumentException(new NullPointerException("The class type cannot be null"));
		
		return new Settings<M>(type, signature, arguments);
	}
	
	/**
	 * Creates a {@link Settings} object with the specified type, signature, and arguments
	 * 
	 * @param type The class type of the loaded object
	 * 
	 * @return A {@link Settings} object with the specified type, signature, and arguments
	 */
	public static <M> Settings<M> of(Class<M> type) {
		return of(type, null, null);
	}
}