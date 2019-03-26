/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my;

import base.Action;
import blocksworld.Block;
import blocksworld.Element;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Dell
 */
public class MyLongTermAction  extends Element<Block> implements Action
{
	/**
	 * The type of the action.
	 * 
	 * @author Andrei Olaru
	 */
	@SuppressWarnings("javadoc")
    public static enum MyType implements Element.ElementType {
		FIND(1),
		
		BOTTOMIZE(1),
			
		STACK(2),
                
                SCAN(1);
		
		int args;
		
		private MyType()
		{
			args = 0;
		}
		
		private MyType(int arguments)
		{
			args = arguments;
		}
		
		@Override
		public int getArgumentNumber()
		{
			return args;
		}
	}
	
        
	/**
	 * Constructor for actions with no arguments.
	 * 
	 * @param type
	 *            - the type of the action.
	 * @throws IllegalArgumentException
	 *             if the given type has a different number of arguments.
	 */
	public MyLongTermAction(MyType type)
	{
		super(type);
	}
	
	/**
	 * Constructor for actions with one argument.
	 * 
	 * @param type
	 *            - type of the action.
	 * @param argument
	 *            - the argument of the action.
	 * @throws IllegalArgumentException
	 *             if the given type has a different number of arguments.
	 */
	public MyLongTermAction(MyType type, Block argument)
	{
		super(type, argument);
	}
	
	/**
	 * Constructor for actions with two arguments.
	 * 
	 * @param type
	 *            - type of the action.
	 * @param firstArgument
	 *            - first argument of the action.
	 * @param secondArgument
	 *            - second argument of the action.
	 * @throws IllegalArgumentException
	 *             if the given type has a different number of arguments.
	 */
	public MyLongTermAction(MyType type, Block firstArgument, Block secondArgument)
	{
		super(type, firstArgument, secondArgument);
	}
	
	/**
	 * @return the type of the action.
	 */
	public MyType getType()
	{
		return (MyType) elementType;
	}

}

