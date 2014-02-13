/*
 * Copyright (c) 2010, Frederik Vanhoutte This library is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * http://creativecommons.org/licenses/LGPL/2.1/ This library is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package wblut.hemesh;

import java.util.List;

import javolution.util.FastList;
import wblut.WB_Epsilon;
import wblut.geom.WB_Normal3d;
import wblut.geom.WB_Point3d;
import wblut.geom.WB_VertexType3D;

// TODO: Auto-generated Javadoc
/**
 * Vertex element of half-edge mesh.
 * 
 * @author Frederik Vanhoutte (W:Blut)
 * 
 */
public class HE_Vertex extends WB_Point3d {
    
    /** Halfedge associated with this vertex. */
    private HE_Halfedge _halfedge;
    
    /** Static vertex key counter. */
    protected static int _currentKey;
    
    /** Unique vertex key. */
    protected final Integer _key;
    
    /** General purpose label. */
    protected int label;
    
    private double u, v;
    
    /**
     * Instantiates a new HE_Vertex.
     */
    public HE_Vertex() {
        _key = new Integer(_currentKey);
        _currentKey++;
        label = -1;
        u = v = 0;
    }
    
    /**
     * Instantiates a new HE_Vertex at position x, y, z.
     * 
     * @param x
     *            x-coordinate of vertex
     * @param y
     *            y-coordinate of vertex
     * @param z
     *            z-coordinate of vertex
     */
    public HE_Vertex(final double x, final double y, final double z) {
        super(x, y, z);
        _key = new Integer(_currentKey);
        _currentKey++;
        label = -1;
        u = v = 0;
    }
    
    public HE_Vertex(final double x, final double y, final double z, double u,
            double v) {
        super(x, y, z);
        _key = new Integer(_currentKey);
        _currentKey++;
        label = -1;
        this.u = u;
        this.v = v;
    }
    
    /**
     * Instantiates a new HE_Vertex at position v.
     * 
     * @param v
     *            position of vertex
     */
    public HE_Vertex(final WB_Point3d v) {
        super(v);
        _key = new Integer(_currentKey);
        _currentKey++;
        label = -1;
        u = this.v = 0;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see wblut.geom.Point3D#get()
     */
    @Override
    public HE_Vertex get() {
        return new HE_Vertex(x, y, z, u, v);
    }
    
    /**
     * Get halfedge associated with this vertex.
     * 
     * @return halfedge
     */
    public HE_Halfedge getHalfedge() {
        return _halfedge;
    }
    
    /**
     * Sets the halfedge associated with this vertex.
     * 
     * @param halfedge
     *            the new halfedge
     */
    public void setHalfedge(final HE_Halfedge halfedge) {
        _halfedge = halfedge;
    }
    
    public void setU(double u) {
        this.u = u;
        
    }
    
    public void setV(double v) {
        this.v = v;
        
    }
    
    public double u() {
        return u;
        
    }
    
    public double v() {
        return v;
        
    }
    
    public float uf() {
        return (float) u;
        
    }
    
    public float vf() {
        return (float) v;
        
    }
    
    /**
     * Set position to v.
     * 
     * @param v
     *            position
     */
    public void set(final HE_Vertex v) {
        super.set(v);
    }
    
    /**
     * Get vertex normal. Returns stored value if update status is true.
     * 
     * @return normal
     */
    public WB_Normal3d getVertexNormal() {
        
        if (_halfedge == null) {
            return null;
        }
        HE_Halfedge he = _halfedge;
        final WB_Normal3d _normal = new WB_Normal3d();
        final FastList<WB_Normal3d> normals = new FastList<WB_Normal3d>();
        do {
            if (he.getFace() != null) {
                final WB_Normal3d fn = he.getFace().getFaceNormal();
                normals.add(fn);
            }
            he = he.getNextInVertex();
        } while (he != _halfedge);
        for (int i = 0; i < normals.size(); i++) {
            final WB_Normal3d ni = normals.get(i);
            boolean degenerate = false;
            for (int j = i + 1; j < normals.size(); j++) {
                final WB_Normal3d nj = normals.get(j);
                if (ni.isParallel(nj)) {
                    degenerate = true;
                    break;
                }
            }
            if (!degenerate) {
                _normal.add(ni);
                
            }
            
        }
        
        _normal.normalize();
        return _normal;
    }
    
    /**
     * Get vertex type. Returns stored value if update status is true.
     * 
     * @return HE.VertexType.FLAT: vertex is flat in all faces,
     *         HE.VertexType.CONVEX: vertex is convex in all faces,
     *         HE.VertexType.CONCAVE: vertex is concave in all faces,
     *         HE.VertexType.FLATCONVEX: vertex is convex or flat in all faces,
     *         HE.VertexType.FLATCONCAVE: vertex is concave or flat in all
     *         faces, HE.VertexType.SADDLE: vertex is convex and concave in at
     *         least one face each
     */
    public WB_VertexType3D getVertexType() {
        
        if (_halfedge == null) {
            return null;
        }
        HE_Halfedge he = _halfedge;
        int nconcave = 0;
        int nconvex = 0;
        int nflat = 0;
        do {
            HE_Face f = he.getFace();
            if (f == null) {
                f = he.getPair().getFace();
            }
            final WB_Point3d v = he.getNextInFace().getVertex().get();
            v.sub(he.getVertex());
            he = he.getNextInVertex();
            HE_Face fn = he.getFace();
            if (fn == null) {
                fn = he.getPair().getFace();
            }
            final WB_Normal3d c = f.getFaceNormal().cross(fn.getFaceNormal());
            
            final double d = v.dot(c);
            if (Math.abs(d) < WB_Epsilon.EPSILON) {
                nflat++;
            }
            else if (d < 0) {
                nconcave++;
            }
            else {
                nconvex++;
            }
            
        } while (he != _halfedge);
        if (nconcave > 0) {
            if (nconvex > 0) {
                return WB_VertexType3D.SADDLE;
            }
            else {
                if (nflat > 0) {
                    return WB_VertexType3D.FLATCONCAVE;
                }
                else {
                    return WB_VertexType3D.CONCAVE;
                }
            }
        }
        else if (nconvex > 0) {
            if (nflat > 0) {
                return WB_VertexType3D.FLATCONVEX;
            }
            else {
                return WB_VertexType3D.CONVEX;
            }
        }
        
        return WB_VertexType3D.FLAT;
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see wblut.geom.Point3D#toString()
     */
    @Override
    public String toString() {
        return "HE_Vertex key: " + key() + " [x=" + x + ", y=" + y + ", z=" + z
                + "]";
    }
    
    /**
     * Clear halfedge.
     */
    public void clearHalfedge() {
        _halfedge = null;
    }
    
    /**
     * Get key.
     * 
     * @return key
     */
    public Integer key() {
        return _key;
    }
    
    /**
     * Sets the label.
     * 
     * @param lab
     *            the new label
     */
    public void setLabel(final int lab) {
        label = lab;
        
    }
    
    /**
     * Gets the label.
     * 
     * @return the label
     */
    public int getLabel() {
        return label;
        
    }
    
    /**
     * Get halfedges in vertex.
     * 
     * @return halfedges
     */
    public List<HE_Halfedge> getHalfedgeStar() {
        final List<HE_Halfedge> vhe = new FastList<HE_Halfedge>();
        if (getHalfedge() == null) {
            return vhe;
        }
        HE_Halfedge he = getHalfedge();
        do {
            if (!vhe.contains(he)) {
                vhe.add(he);
            }
            he = he.getNextInVertex();
        } while (he != getHalfedge());
        return vhe;
    }
    
    /**
     * Get edges in vertex.
     * 
     * @return edges
     */
    public List<HE_Edge> getEdgeStar() {
        
        final List<HE_Edge> ve = new FastList<HE_Edge>();
        if (getHalfedge() == null) {
            return ve;
        }
        HE_Halfedge he = getHalfedge();
        do {
            if (!ve.contains(he.getEdge())) {
                ve.add(he.getEdge());
            }
            he = he.getNextInVertex();
        } while (he != getHalfedge());
        return ve;
    }
    
    /**
     * Get faces in vertex.
     * 
     * @return faces
     */
    public List<HE_Face> getFaceStar() {
        final List<HE_Face> vf = new FastList<HE_Face>();
        if (getHalfedge() == null) {
            return vf;
        }
        HE_Halfedge he = getHalfedge();
        do {
            if (he.getFace() != null) {
                if (!vf.contains(he.getFace())) {
                    vf.add(he.getFace());
                }
            }
            he = he.getNextInVertex();
        } while (he != getHalfedge());
        return vf;
    }
    
    /**
     * Get neighboring vertices.
     * 
     * @return neighbors
     */
    public List<HE_Vertex> getNeighborVertices() {
        final List<HE_Vertex> vv = new FastList<HE_Vertex>();
        if (getHalfedge() == null) {
            return vv;
        }
        HE_Halfedge he = getHalfedge();
        do {
            final HE_Halfedge hen = he.getNextInFace();
            if ((hen.getVertex() != this) && (!vv.contains(hen.getVertex()))) {
                vv.add(hen.getVertex());
            }
            he = he.getNextInVertex();
        } while (he != getHalfedge());
        return vv;
    }
    
    /**
     * Gets the neighbors as points.
     * 
     * @return the neighbors as points
     */
    public WB_Point3d[] getNeighborsAsPoints() {
        final WB_Point3d[] vv = new WB_Point3d[getVertexOrder()];
        if (getHalfedge() == null) {
            return vv;
        }
        HE_Halfedge he = getHalfedge();
        int i = 0;
        do {
            vv[i] = he.getEndVertex();
            i++;
            he = he.getNextInVertex();
        } while (he != getHalfedge());
        return vv;
    }
    
    /**
     * Get number of edges in vertex.
     * 
     * @return number of edges
     */
    public int getVertexOrder() {
        
        int result = 0;
        if (getHalfedge() == null) {
            return 0;
        }
        HE_Halfedge he = getHalfedge();
        do {
            
            result++;
            he = he.getNextInVertex();
        } while (he != getHalfedge());
        
        return result;
        
    }
    
    /**
     * Get area of faces bounding vertex.
     * 
     * @return area
     */
    public double getVertexArea() {
        if (getHalfedge() == null) {
            return 0;
        }
        double result = 0;
        int n = 0;
        HE_Halfedge he = getHalfedge();
        do {
            if (he.getFace() != null) {
                result += he.getFace().getFaceArea();
                n++;
            }
            he = he.getNextInVertex();
        } while (he != getHalfedge());
        return result / n;
        
    }
    
    /**
     * Checks if is boundary.
     * 
     * @return true, if is boundary
     */
    public boolean isBoundary() {
        HE_Halfedge he = _halfedge;
        do {
            if (he.getFace() == null)
                return true;
            he = he.getNextInVertex();
        } while (he != _halfedge);
        return false;
        
    }
    
}
