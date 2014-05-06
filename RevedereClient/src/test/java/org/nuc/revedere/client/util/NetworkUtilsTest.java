package org.nuc.revedere.client.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class NetworkUtilsTest {
    @Test
    public void testValidAddress() {
        final String addressToValidate = "192.168.101.100:25255";
        assertTrue(NetworkUtils.validAddress(addressToValidate));
    }

    @Test
    public void testInvalidIPAddress() {
        final String addressToValidate = "192.168.101.:25255";
        assertFalse(NetworkUtils.validAddress(addressToValidate));
    }

    @Test
    public void testInvalidPort() {
        final String addressToValidate = "192.168.101.100:123456";
        assertFalse(NetworkUtils.validAddress(addressToValidate));
    }

    @Test
    public void testMissingPort() {
        String addressToValidate = "192.168.101.100:";
        assertFalse(NetworkUtils.validAddress(addressToValidate));

        addressToValidate = "192.168.101.100";
        assertFalse(NetworkUtils.validAddress(addressToValidate));
    }

    @Test
    public void testMissingAddress() {
        String addressToValidate = ":1234";
        assertFalse(NetworkUtils.validAddress(addressToValidate));

        addressToValidate = "1234";
        assertFalse(NetworkUtils.validAddress(addressToValidate));
    }

    @Test
    public void testNullAddress() {
        assertFalse(NetworkUtils.validAddress(null));
    }

    @Test(expected = Exception.class)
    public void testGetIPAddress_InvalidAddress() throws Exception {
        final String invalidAddress = "192.168.0.1";
        NetworkUtils.extractIP(invalidAddress);
    }

    @Test
    public void testGetIPAddress_validAddress() throws Exception {
        final String ipAddress = "192.168.1.1";
        final String lowerValidAddress = String.format("%s:%d", ipAddress, NetworkUtils.MINIMUM_PORT_VALUE);
        assertEquals(ipAddress, NetworkUtils.extractIP(lowerValidAddress));
        
        final String upperValidAddress = String.format("%s:%d", ipAddress, NetworkUtils.MAXIMUM_PORT_VALUE);
        assertEquals(ipAddress, NetworkUtils.extractIP(upperValidAddress));
    }

    @Test(expected = Exception.class)
    public void testGetPort_InvalidAddress() throws Exception {
        final String invalidAddress = "192.168.0.1";
        NetworkUtils.extractPort(invalidAddress);
    }

    @Test(expected = Exception.class)
    public void testGetPort_OutOfRangePort() throws Exception {
        String invalidAddress = "192.168.0.1:" + (NetworkUtils.MINIMUM_PORT_VALUE - 1);
        NetworkUtils.extractPort(invalidAddress);

        invalidAddress = "192.168.0.1:0";
        NetworkUtils.extractPort(invalidAddress);

        invalidAddress = "192.168.0.1:" + (NetworkUtils.MAXIMUM_PORT_VALUE + 1);
        NetworkUtils.extractPort(invalidAddress);
    }

    @Test
    public void testGetPort_validAddress() throws Exception {
        final String ipAddress = "192.168.1.1";
        final int port = 2552;
        final String validAddress = String.format("%s:%d", ipAddress, port);
        assertEquals(port, NetworkUtils.extractPort(validAddress));
    }
}
