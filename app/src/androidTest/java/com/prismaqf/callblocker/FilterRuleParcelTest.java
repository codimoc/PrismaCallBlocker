package com.prismaqf.callblocker;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import com.prismaqf.callblocker.rules.FilterRule;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class FilterRuleParcelTest {

    @Test
    public void TestParcelable() {
        FilterRule fr1 = new FilterRule("first","A filter");
        fr1.addPattern("123");
        fr1.addPattern("4*56");
        Parcel parcel = Parcel.obtain();
        fr1.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        FilterRule fr2 = FilterRule.CREATOR.createFromParcel(parcel);
        assertEquals("Equal after Prcelization", fr1, fr2);
    }
}
