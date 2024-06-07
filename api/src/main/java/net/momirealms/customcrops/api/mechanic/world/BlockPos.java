/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customcrops.api.mechanic.world;

import java.util.Objects;

public class BlockPos {

    private final int position;

    public BlockPos(int position) {
        this.position = position;
    }

    public BlockPos(int x, int y, int z) {
        this.position = ((x & 0xF) << 28) | ((z & 0xF) << 24) | (y & 0xFFFFFF);
    }

    public static BlockPos getByLocation(SimpleLocation location) {
        return new BlockPos(location.getX() % 16, location.getY(), location.getZ() % 16);
    }

    public SimpleLocation getLocation(String world, ChunkPos coordinate) {
        return new SimpleLocation(world, coordinate.x() * 16 + getX(), getY(), coordinate.z() * 16 + getZ());
    }

    public int getPosition() {
        return position;
    }

    public int getX() {
        return (position >> 28) & 0xF;
    }

    public int getZ() {
        return (position >> 24) & 0xF;
    }

    public int getSectionID() {
        return (int) Math.floor((double) getY() / 16);
    }

    public int getY() {
        int y = position & 0xFFFFFF;
        if ((y & 0x800000) != 0) {
            y |= 0xFF000000;
        }
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPos blockPos = (BlockPos) o;
        return position == blockPos.position;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position);
    }

    @Override
    public String toString() {
        return "BlockPos{" +
                "x=" + getX() +
                "y=" + getY() +
                "z=" + getZ() +
                '}';
    }
}
