package com.wwb.leetcode.tags.dc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Given a string that contains only digits 0-9 and a target value,
 * return all possibilities to add binary operators (not unary) +, -, or * between the digits so they evaluate to the target value.
 *
 * Examples:
 * "123", 6 -> ["1+2+3", "1*2*3"]
 * "232", 8 -> ["2*3+2", "2+3*2"]
 * "105", 5 -> ["1*0+5","10-5"]
 * "00", 0 -> ["0+0", "0-0", "0*0"]
 * "3456237490", 9191 -> []
 */
public class No282 {

    public List<String> addOperators(String num, int target) {
        if(num == null || num.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        addOperators(result, "", num, target, 0, 0, 0);
        return result;
    }
    public void addOperators(List<String> result, String path, String num, int target, int position, long evaluatedNumber, long multiplier){
        if(position == num.length()) {
            if(target == evaluatedNumber) {
                result.add(path);
            }
            return;
        }
        for(int i = position; i < num.length(); i++){
            if(i != position && num.charAt(position) == '0') {
                break;
            }
            long currentValue = Long.parseLong(num.substring(position, i + 1));
            if(position == 0) {
                addOperators(result, path + currentValue, num, target, i + 1, currentValue, currentValue);
            } else{
                addOperators(result, path + "+" + currentValue, num, target, i + 1, evaluatedNumber + currentValue , currentValue);

                addOperators(result, path + "-" + currentValue, num, target, i + 1, evaluatedNumber -currentValue, -currentValue);

                addOperators(result, path + "*" + currentValue, num, target, i + 1, evaluatedNumber - multiplier + multiplier * currentValue, multiplier * currentValue );
            }
        }
    }
}
