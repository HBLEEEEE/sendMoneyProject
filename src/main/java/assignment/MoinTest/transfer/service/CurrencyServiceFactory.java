package assignment.MoinTest.transfer.service;


import assignment.MoinTest.transfer.dto.Forex;
import assignment.MoinTest.transfer.entity.BaseAndUnit;
import assignment.MoinTest.transfer.entity.FeeRateAmount;
import assignment.MoinTest.transfer.entity.TargetCurrencyTypeEnum;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Currency;

public class CurrencyServiceFactory {

    private static double jpyDigits = Currency.getInstance("JPY").getDefaultFractionDigits();
    private static double usdDigits = Currency.getInstance("USD").getDefaultFractionDigits();
    private static double jpyRound = Math.pow(10.0, jpyDigits);
    private static double usdRound = Math.pow(10.0, usdDigits);


    public static FeeRateAmount calculate(TargetCurrencyTypeEnum targetCurrencyTypeEnum, double amount){

        if (targetCurrencyTypeEnum==TargetCurrencyTypeEnum.JPY){
            return jpyCalculate(amount);
        } else if (targetCurrencyTypeEnum == TargetCurrencyTypeEnum.USD) {
            return usdCalculate(amount);
        }else {
            return null;
        }
    }

    private static FeeRateAmount jpyCalculate(double amount){
        BaseAndUnit baseAndUnit = getBaseAndUnit("JPY");
        double exchangeRate = baseAndUnit.getTargetBase();
        double exchangeUnit = baseAndUnit.getTargetUnit();
        double usdRate = baseAndUnit.getUsdBase();
        double usdUnit = baseAndUnit.getUsdUnit();

        double fee = 3000;
        double totalFee = amount * 0.005 + fee;
        double usdSendAmount = Math.round(amount * usdUnit / usdRate * usdRound) / usdRound;
        double targetAmount = (amount-totalFee) * exchangeUnit / exchangeRate;
        targetAmount = Math.round(targetAmount * jpyRound) / jpyRound;

        FeeRateAmount feeRateAmount = new FeeRateAmount();
        feeRateAmount.setFee(fee);
        feeRateAmount.setUsdRate(usdRate);
        feeRateAmount.setTargetRate(exchangeRate);
        feeRateAmount.setUsdSendAmount(usdSendAmount);
        feeRateAmount.setTargetAmount(targetAmount);

        return feeRateAmount;
    }

    private static FeeRateAmount usdCalculate(double amount){
        BaseAndUnit baseAndUnit = getBaseAndUnit("USD");
        double usdRate = baseAndUnit.getUsdBase();
        double usdUnit = baseAndUnit.getUsdUnit();

        double fee;
        double totalFee;
        if (amount>1000000){
            fee = 3000;
            totalFee = amount * 0.001 + fee;
        }else{
            fee = 1000;
            totalFee = amount * 0.002 + fee;
        }

        double usdSendAmount = Math.round(amount * usdUnit / usdRate * usdRound) / usdRound;
        double targetAmount = (amount-totalFee) * usdUnit / usdRate;
        targetAmount = Math.round(targetAmount * usdRound) / usdRound;

        FeeRateAmount feeRateAmount = new FeeRateAmount();
        feeRateAmount.setFee(fee);
        feeRateAmount.setUsdRate(usdRate);
        feeRateAmount.setTargetRate(usdRate);
        feeRateAmount.setUsdSendAmount(usdSendAmount);
        feeRateAmount.setTargetAmount(targetAmount);

        return feeRateAmount;
    }

    public static BaseAndUnit getBaseAndUnit(String targetCurrency){
        String target = "FRX.KRW" + targetCurrency + ",FRX.KRWUSD";

        URI uri = UriComponentsBuilder
                .fromUriString("https://quotation-api-cdn.dunamu.com:443")
                .path("/v1/forex/recent")
                .queryParam("codes",target)
                .encode()
                .build()
                .toUri();

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Forex[]> result = restTemplate.getForEntity(uri, Forex[].class);
        Forex[] forexArray = result.getBody();

        BaseAndUnit baseAndUnit = new BaseAndUnit();
        baseAndUnit.setTargetBase(forexArray[0].getBasePrice());
        baseAndUnit.setTargetUnit(forexArray[0].getCurrencyUnit());
        baseAndUnit.setUsdBase(forexArray[1].getBasePrice());
        baseAndUnit.setUsdUnit(forexArray[1].getCurrencyUnit());

        return baseAndUnit;
    }



}
