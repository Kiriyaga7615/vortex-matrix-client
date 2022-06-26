package cis.matrixclient.util.math;

import java.util.*;

public class MathUtils {
    public static int closest(int value, int one, int two){
        int v1 = one - value;
        int v2 = two - value;

        if (v1 > v2) return two;
        else if (v1 == v2) return one;
        else return one;
    }

    public static int closest(int value, int[] numbers){
        Map<Integer, Integer> map = new HashMap();
        for (int i = 0; i < numbers.length; i++){
            map.put(Math.abs(numbers[i] - value), numbers[i]);
        }
        List<Integer> keys = new ArrayList(map.keySet());
        Collections.sort(keys);
        return map.get(keys.get(0));
    }

    public static float closest(float value, float[] numbers){
        Map<Float, Float> map = new HashMap();
        for (int i = 0; i < numbers.length; i++){
            map.put((numbers[i] - value), numbers[i]);
        }
        List<Float> keys = new ArrayList(map.keySet());
        Collections.sort(keys);
        return map.get(keys.get(0));
    }

    public static float findClosest(float arr[], float target)
    {
        int n = arr.length;
        if (target <= arr[0])
            return arr[0];
        if (target >= arr[n - 1])
            return arr[n - 1];
        int i = 0, j = n, mid = 0;
        while (i < j) {
            mid = (i + j) / 2;

            if (arr[mid] == target)
                return arr[mid];
            if (target < arr[mid]) {
                if (mid > 0 && target > arr[mid - 1])
                    return getClosest(arr[mid - 1],
                            arr[mid], target);
                j = mid;
            }
            else {
                if (mid < n-1 && target < arr[mid + 1])
                    return getClosest(arr[mid],
                            arr[mid + 1], target);
                i = mid + 1;
            }
        }
        return arr[mid];
    }

    public static float getClosest(float val1, float val2,
                                 float target)
    {
        if (target - val1 >= val2 - target)
            return val2;
        else
            return val1;
    }

    public static int clamp(int value, int min, int max) {
        if (value < min) return min;
        return Math.min(value, max);
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) return min;
        return Math.min(value, max);
    }

    public static double clamp(double value, double min, double max) {
        if (value < min) return min;
        return Math.min(value, max);
    }
}
