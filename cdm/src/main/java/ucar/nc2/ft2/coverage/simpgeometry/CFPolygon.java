package ucar.nc2.ft2.coverage.simpgeometry;

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * A CF 1.8 compliant Polygon
 * for use with Simple Geometries.
 * Can also represent multipolygons.
 * 
 * @author wchen@usgs.gov
 *
 */
public class CFPolygon implements Polygon  {

	private List<CFPoint> points;	// a list of the constitutent points of the Polygon, connected in ascending order as in the CF convention
	private CFPolygon next;	// if non-null, next refers to the next line part of a multi-polygon
	private CFPolygon prev;	// if non-null, prev refers to the previous line part of a multi-polygon
	private CFPolygon interior_ring; // the polygon that makes up an interior ring, if any
	private Array data;	// data array associated with the polygon
	
	/**
	 * Get the list of points which constitute the polygon.
	 * 
	 * @return points
	 */
	public List<CFPoint> getPoints() {
		return points;
	}

	/**
	 * Get the data associated with this Polygon
	 * 
	 * @return data
	 */
	public Array getData() {
		return data;
	}
	
	/**
	 * Get the next polygon in the sequence of multi-polygons
	 * 
	 * @return next polygon in the same multipolygon if any, otherwise null
	 */
	public CFPolygon getNext() {
		return next;
	}
	
	/**
	 * Get the previous polygon in the sequence of multi-polygons
	 * 
	 * @return previous polygon in the same multipolygon if any, otherwise null
	 */
	public CFPolygon getPrev() {
		return prev;
	}
	
	/**
	 * Get this polygon's interior ring.
	 * 
	 * @return previous interior ring as a polygon if any, otherwise null
	 */
	public CFPolygon getInteriorRing() {
		return interior_ring;
	}
	
	/**
	 * Add a point to this polygon's points list
	 * 
	 */
	public void addPoint(double x, double y) {
		CFPoint pt_prev = null;
		
		if(points.size() > 0) {
			pt_prev = points.get(points.size() - 1);
		}
		
		this.points.add(new CFPoint(x, y, pt_prev, null));
	}
	
	/**
	 * Set the data associated with this Polygon
	 * 
	 * @param data - array of data to set to
	 */
	public void setData(Array data) {
		this.data = data;
	}
	
	/**
	 * Sets the next polygon which make up the multipolygon which this polygon is a part of.
	 * Automatically connects the other polygon to this polygon as well.
	 */
	public void setNext(CFPolygon next) {
		this.next = next;
		
		if(next != null) {
			next.setPrevOnce(this);
		}
	}
	
	private void setNextOnce(CFPolygon next) {
		this.next = next;
	}

	/**
	 * Sets the previous polygon which makes up the multipolygon which this polygon is a part of.
	 * Automatically connect the other polygon to this polygon as well.
	 */
	public void setPrev(CFPolygon prev) {
		this.prev = prev;
		
		if(prev != null) {
			prev.setNextOnce(this);
		}
	}
	
	private void setPrevOnce(CFPolygon prev) {
		this.prev = prev;
	}
	
	/**
	 *  Simply sets the interior ring of the polygon.
	 * 
	 */
	public void setInteriorRing(CFPolygon interior) {
		this.interior_ring = interior;
	}
	
	/**
	 * Given a dataset, variable and index, automatically constructs a new Polygon
	 * 
	 */
	public CFPolygon(NetcdfDataset dataset, Variable polyvar, int index)
	{
		this.points = new ArrayList<CFPoint>();
		Array xPts = null;
		Array yPts = null;

		List<Variable> vars = dataset.getVariables();
		//List<CoordinateAxis> axes = dataset.getCoordinateAxes();
		Variable x = null; Variable y = null;
		
		// Look for x and y
		
		for(Variable ax : vars){
			
			if(ax.findAttValueIgnoreCase("axis", "").equalsIgnoreCase("X")) x = ax;
			if(ax.findAttValueIgnoreCase("axis", "").equalsIgnoreCase("Y")) y = ax;
		}
		
		try {
			xPts = x.read( ":").reduce();
			yPts = y.read( ":").reduce();
		
		} catch (IOException e) {
			
			e.printStackTrace();
			xPts = null;
			yPts = null;
		} catch (InvalidRangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// This will be revised to get a single polygon
		IndexIterator itr_x = xPts.getIndexIterator();
		IndexIterator itr_y = yPts.getIndexIterator();
		
		// x and y should have the same shape, will add some handling on this
		while(itr_x.hasNext())
		{
			this.addPoint(itr_x.getDoubleNext(), itr_y.getDoubleNext());
		}
		
		
		// Now set the Data
		try {
			this.setData(polyvar.read());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// still things to set
		this.next = null;
		this.prev = null;
		this.interior_ring = null;
	}
	
	
	/**
	 * Constructs an empty polygon with nothing in it using an Array List.
	 */
	public CFPolygon() {
		this.points = new ArrayList<CFPoint>();
		this.next = null;
		this.prev = null;
		this.interior_ring = null;
		this.data = null;
	}
	
	/**
	 * Constructs a new polygon whose points constitute the points passed in.
	 */
	public CFPolygon(List<CFPoint> points) {
		this.points = points;
		this.next = null;
		this.prev = null;
		this.interior_ring = null;
		this.data = null;
	}
}
