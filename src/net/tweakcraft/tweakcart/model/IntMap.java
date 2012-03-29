/*
 * Copyright (c) 2012.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.tweakcraft.tweakcart.model;

import org.bukkit.Material;

import java.util.Arrays;

/**
 * *hihi* Edited with eclipse
 *
 * @author Edoxile
 */
public class IntMap {
    public static final int materialSize = Material.values().length;
    public static final int mapSize = materialSize + 67;
    private int[] mapData;

    public IntMap() {
        mapData = new int[mapSize];
    }

    private IntMap(int[] data) {
        if (data.length != (mapSize)) {
            mapData = new int[mapSize];
        } else {
            mapData = data;
        }
    }

    public static boolean isAllowedMaterial(int id, byte data) {
        int intLocation = IntMap.getIntIndex(id, data);
        return intLocation != -1;
    }

    public int getInt(int id, byte data) {
        int intLocation = IntMap.getIntIndex(id, data);

        if (intLocation == -1 || intLocation >= mapSize) {
            return 0;
        }

        return mapData[intLocation];
    }

    public int getInt(Material m, byte data) {
        int intLocation = IntMap.getIntIndex(m, data);

        if (intLocation == -1) {
            return 0;
        }

        return mapData[intLocation];
    }

    public boolean setInt(Material m, byte data, int value) {
        return setInt(m.getId(), data, value);
    }

    public boolean setInt(int id, byte data, int value) {
        if (hasDataValue(id) && data == (byte) -1) {
            setDataRange(id, (byte) 0, (byte) 15, value);
        } else {
            int intLocation = IntMap.getIntIndex(id, data);
            if (intLocation == -1) {
                return false;
            }
            mapData[intLocation] = value;
        }
        return true;
    }

    public static int getIntIndex(int id, byte data) {
        return getIntIndex(Material.getMaterial(id), data);
    }

    private static int getIntIndex(Material m, byte data) {
        if (m == null) {
            return -1;
        }
        switch (data) {
            case 0:
                //Alle items waarop we .ordinal kunnen doen
                return m.ordinal();
            default:
                //Alle andere gevallen
                switch (m) {
                    case SAPLING:
                        if (data < 4)
                            return materialSize + (int) data;
                        else
                            return -1;
                    case LOG:
                        if (data < 4)
                            return materialSize + (int) data + 3;
                        else
                            return -1;
                    case LEAVES:
                        if (data < 3)
                            return materialSize + (int) data + 6;
                        else
                            return -1;
                    case WOOL:
                        if (data < 16)
                            return materialSize + (int) data + 21;
                        else
                            return -1;
                    case INK_SACK:
                        if (data < 16)
                            return materialSize + (int) data + 36;
                        else
                            return -1;
                    case COAL:
                        if (data < 2)
                            return materialSize + (int) data + 51;
                        else
                            return -1;
                    case STEP:
                        if (data < 7)
                            return materialSize + (int) data + 52;
                        else
                            return -1;
                    case LONG_GRASS:
                        if (data < 3)
                            return materialSize + (int) data + 58;
                        else
                            return -1;
                    case WOOD:
                        if (data < 4)
                            return materialSize + (int) data + 61;
                        else
                            return -1;
                    case SMOOTH_BRICK:
                        if (data < 4)
                            return materialSize + (int) data + 64;
                        else
                            return -1;
                    case SANDSTONE:
                        if (data < 3)
                            return materialSize + (int) data + 66;
                    default:
                        return m.ordinal();
                }
        }


    }

    private boolean hasDataValue(int id) {
        switch (id) {
            case 6:
            case 17:
            case 18:
            case 35:
            case 44:
            case 263:
            case 351:
                return true;
            default:
                return false;
        }
    }

    @Deprecated
    public void combine(IntMap otherMap) {
        for (int index = 0; index < mapData.length; index++) {
            if (otherMap.mapData[index] != 0)
                mapData[index] = otherMap.mapData[index];
        }
    }

    public boolean setRange(int startId, byte startdata, int endId, byte enddata, int value) {
        if (startdata < -1 || enddata < -1 || startId > endId
                || (startdata > 0 && !hasDataValue(startId)) || (enddata > 0 && !hasDataValue(endId))
                || !isAllowedMaterial(startId, startdata) || !isAllowedMaterial(endId, enddata))
            return false;
        if (startId < endId) {
            if (startdata >= 0 && enddata >= 0) {
                setDataRange(startId, startdata, (byte) 15, value);
                startId++;
                setDataRange(endId, (byte) 0, enddata, value);
                endId--;
            } else if (startdata == -1 && enddata >= 0) {
                setDataRange(endId, (byte) 0, enddata, value);
                endId--;
            } else if (startdata >= 0 && enddata == -1) {
                setDataRange(startId, startdata, (byte) 15, value);
                startId++;
            }
            while (startId <= endId) {
                if (hasDataValue(startId)) {
                    setDataRange(startId, (byte) 0, (byte) 15, value);
                } else {
                    setInt(startId, (byte) 0, value);
                }
                do {
                    startId++;
                } while (!isAllowedMaterial(startId, (byte) 0));
            }
            return true;
        } else if (startId == endId) {
            if (startdata < enddata && hasDataValue(startId)) {
                setDataRange(startId, startdata, enddata, value);
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    private boolean setDataRange(int id, byte start, byte end, int amount) {
        if (!hasDataValue(id)) {
            return false;
        }
        for (byte data = start; data <= end; data++) {
            int key = getIntIndex(id, data);
            if (key == -1) {
                break;
            }
            mapData[key] = amount;
        }
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof IntMap) {
            IntMap otherMap = (IntMap) other;
            for (int index = 0; index < mapData.length; index++) {
                if (mapData[index] != otherMap.mapData[index])
                    return false;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mapData);
    }

    @Override
    public IntMap clone() throws CloneNotSupportedException {
        super.clone();
        return new IntMap(mapData.clone());
    }

    @Override
    public String toString() {
        String str = "{\n";
        for (int i = 0; i < mapData.length; i++) {
            if (mapData[i] != 0) {
                str += " [" + i + "] -> " + mapData[i] + "\n";
            }
        }
        str += "}";
        return str;
    }

    public void fillAll() {
        fillAll(false);
    }

    @Deprecated
    public void fillAll(boolean negative) {
        int value = negative ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int i = 0; i < mapData.length; i++) {
            mapData[i] = value;
        }

    }
}