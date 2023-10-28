package com.htc.mds.util;

import com.htc.mds.model.AuthClientInfo;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommUtils {

    public static String[] getUserListSplit(String recipients) {
        String[] userArr;
        if (recipients.contains(ConstValue.RecipientsSplit)) {
            userArr = org.springframework.util.StringUtils.delimitedListToStringArray(recipients, ConstValue.RecipientsSplit);

        } else {
            userArr = new String[]{recipients};
        }

        List<String> list = new ArrayList<String>();
        for (String item : userArr) {
            list.add(item);
        }

        Set<String> sets = new HashSet<String>();
        sets.addAll(list);

        String[] arr = sets.toArray(new String[0]);
        return arr;
    }

    public static String getUserMailString(List<String> recipients) {
        String str = StringUtils.join(recipients, ConstValue.RecipientsSplit.charAt(0));
        return str;
    }

    public static String getClientId() {
        AuthClientInfo principal = (AuthClientInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal != null && principal.isAuthStatus()) {
            return principal.getClientId();
        }
        throw new RuntimeException("client id is null");
    }
}
