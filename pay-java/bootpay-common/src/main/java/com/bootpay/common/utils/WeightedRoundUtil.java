package com.bootpay.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeightedRoundUtil {
    private List<String> channelCodes;
    private List<Integer> channelWeights;
    private int totalWeight;
    private Random random;

    public WeightedRoundUtil(List<String> channelCodes, List<Integer> channelWeights) {
        if (channelCodes.size() != channelWeights.size()) {
            throw new IllegalArgumentException("Number of channel codes must be equal to number of channel weights");
        }

        this.channelCodes = new ArrayList<>(channelCodes);
        this.channelWeights = new ArrayList<>(channelWeights);
        this.totalWeight = calculateTotalWeight(channelWeights);
        this.random = new Random();
    }

    private int calculateTotalWeight(List<Integer> channelWeights) {
        int total = 0;
        for (int weight : channelWeights) {
            total += weight;
        }
        return total;
    }

    public String getNextChannel() {
        int randomValue = random.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (int i = 0; i < channelCodes.size(); i++) {
            cumulativeWeight += channelWeights.get(i);
            if (randomValue < cumulativeWeight) {
                return channelCodes.get(i);
            }
        }

        // This should not happen, but if it does, return the last channel code as a fallback
        return channelCodes.get(channelCodes.size() - 1);
    }

    public static void main(String[] args) {
//        List<String> channelCodes = List.of("8008", "2002", "1001","3003", "7007", "9009");//通道编号
//        List<Integer> channelWeights = List.of(9,9,9,9,9,9);//通道出现的权重
        List<String> channelCodes = List.of("8008");//通道编号
        List<Integer> channelWeights = List.of(9);//通道出现的权重
        WeightedRoundUtil weightedRoundUtil = new WeightedRoundUtil(channelCodes,channelWeights);
        // Example usage
        for (int i = 0; i < 100; i++) {
            String selectedChannel = weightedRoundUtil.getNextChannel();
            System.out.println("Selected channel: " + selectedChannel);
        }
    }



}


