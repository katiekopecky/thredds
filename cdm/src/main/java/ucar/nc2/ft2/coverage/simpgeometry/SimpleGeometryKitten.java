package ucar.nc2.ft2.coverage.simpgeometry;

import ucar.nc2.Variable;
import ucar.nc2.constants.CF;
import ucar.nc2.ft2.coverage.simpgeometry.*;

/**
 * A cat (the animal) trained in finding Simple Geometry.
 * 
 * A Simple Geometry Kitten can go find the beginning and end indicies of
 * a simple geometry X and Y within a variable. But first the Kitten needs a Variable to find it in.
 * The kitten remembers previous adventures so if the kitten is tasked to find a Simple Geometry close to one before it
 * will find it faster.
 * 
 * @author wchen
 *
 */
public class SimpleGeometryKitten {
	
	Variable cat_toy;
	int past_index;
	int previous_last;
	
	public int getBeginning(int index) {
		
		//Test if the last end is the new beginning
		if(index == (past_index + 1 ))
		{
			return previous_last;
		}
		
		
		past_index = index;
	}
	
	public int getEnd(int index) {
		past_index = index;
	}
	
	/**
	 * Call up a new Kitten, the Kitten must be given a Variable though.
	 * 
	 * @param variable Variable to give it
	 */
	public SimpleGeometryKitten(Variable variable) {
		cat_toy = variable;
		cat_toy.findAttribute(CF.GEOMETRY);
		past_index = -3;
		previous_last = -1;
	}
}
