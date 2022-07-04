package com.zpedroo.voltzevents.objects.host;

import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.voltzevents.enums.PreHostChatAction;
import lombok.Data;

import java.math.BigInteger;

@Data
public class PreEventHostEdit {

    private final PreEventHost preEventHost;
    private final PreHostChatAction action;
    private final Currency currency;
    private final BigInteger minimumValue;
}