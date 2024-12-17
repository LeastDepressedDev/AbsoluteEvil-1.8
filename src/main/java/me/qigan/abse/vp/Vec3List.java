package me.qigan.abse.vp;

import net.minecraft.util.Vec3;
import net.minecraft.util.Vector3d;

import java.util.ArrayList;
import java.util.Collection;

public class Vec3List extends ArrayList<Vector3d> {
    public Vector3d vecSum = new Vector3d();

    private void inc(Vector3d vec) {
        vecSum.x += vec.x;
        vecSum.y += vec.y;
        vecSum.z += vec.z;
    }

    private void dec(Vector3d vec) {
        vecSum.x -= vec.x;
        vecSum.y -= vec.y;
        vecSum.z -= vec.z;
    }

    @Override
    public boolean add(Vector3d vec) {
        inc(vec);
        return super.add(vec);
    }

    @Override
    public void add(int index, Vector3d vec) {
        inc(vec);
        super.add(index, vec);
    }

    @Override
    public boolean addAll(Collection<? extends Vector3d> c) {
        for (Vector3d it : c) {
            inc(it);
        }
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Vector3d> c) {
        for (Vector3d it : c) {
            inc(it);
        }
        return super.addAll(index, c);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Vector3d) dec((Vector3d) o);
        return super.remove(o);
    }

    @Override
    public Vector3d remove(int index) {
        dec(this.get(index));
        return super.remove(index);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object it : c) {
            if (it instanceof Vector3d) dec((Vector3d) it);
        }
        return super.removeAll(c);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        for (int i = fromIndex; i <= toIndex; i++) {
            dec(this.get(i));
        }
        super.removeRange(fromIndex, toIndex);
    }

    public Vec3 medium() {
        return new Vec3(vecSum.x/this.size(), vecSum.y/this.size(), vecSum.z/this.size());
    }
}
