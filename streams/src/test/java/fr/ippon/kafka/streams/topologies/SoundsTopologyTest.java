package fr.ippon.kafka.streams.topologies;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static fr.ippon.kafka.streams.utils.Const.WINDOWING_TIME;
import static org.junit.Assert.*;

public class SoundsTopologyTest {

    @Test
    public void test() {
        System.out.println(TimeUnit.SECONDS.toMillis(WINDOWING_TIME));
    }

}