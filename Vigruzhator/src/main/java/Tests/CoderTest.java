package Tests;

import org.example.Vigruzhator.Coder;
import org.junit.Assert;
import org.junit.Test;

public class CoderTest {
    @Test
    public void encodeDecodeTest() {
        String expectedString = "Test String for Encode/Decode";
        String encodedString = null;
        String actualString = null;

        try {
            encodedString = Coder.encodePassword(expectedString);
            actualString = Coder.decodePassword(encodedString);
        } catch (Exception e) {

        }

        Assert.assertEquals(expectedString, actualString);
    }
}