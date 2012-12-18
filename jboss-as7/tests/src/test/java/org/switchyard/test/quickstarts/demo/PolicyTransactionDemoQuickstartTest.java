/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.switchyard.test.quickstarts.demo;

import java.io.IOException;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.test.ArquillianUtil;
import org.switchyard.component.test.mixins.hornetq.HornetQMixIn;
import org.switchyard.test.quickstarts.util.ResourceDeployer;

/**
 * The policy-transaction demo quickstart test.
 */
@RunWith(Arquillian.class)
public class PolicyTransactionDemoQuickstartTest {

    private static final String QUEUE_IN = "policyQSTransacted";
    private static final String QUEUE_IN_NOTX = "policyQSNonTransacted";
    private static final String QUEUE_OUT_A = "queueA";
    private static final String QUEUE_OUT_B = "queueB";
    private static final String QUEUE_OUT_C = "queueC";
    private static final String USER = "guest";
    private static final String PASSWD = "guestp";
    
    private HornetQMixIn _hqMixIn;
    
    @Deployment(testable = false)
    public static JavaArchive createDeployment() throws IOException {
        ResourceDeployer.addQueue(QUEUE_IN);
        ResourceDeployer.addQueue(QUEUE_IN_NOTX);
        ResourceDeployer.addQueue(QUEUE_OUT_A);
        ResourceDeployer.addQueue(QUEUE_OUT_B);
        ResourceDeployer.addQueue(QUEUE_OUT_C);
        ResourceDeployer.addPropertiesUser(USER, PASSWD);
        return ArquillianUtil.createJarDemoDeployment("switchyard-quickstart-demo-policy-transaction");
    }

    @Before
    public void before() throws Exception {
        _hqMixIn = new HornetQMixIn(false)
                            .setUser(USER)
                            .setPassword(PASSWD);
        _hqMixIn.initialize();
    }
    
    @After
    public void after() throws Exception {
        _hqMixIn.uninitialize();
    }
    
    @AfterClass
    public static void afterClass() throws Exception {
        ResourceDeployer.removeQueue(QUEUE_IN);
        ResourceDeployer.removeQueue(QUEUE_IN_NOTX);
        ResourceDeployer.removeQueue(QUEUE_OUT_A);
        ResourceDeployer.removeQueue(QUEUE_OUT_B);
        ResourceDeployer.removeQueue(QUEUE_OUT_C);
    }
    
    @Test
    public void testRollbackA() throws Exception {
        String command = "rollback.A";
        
        Session session = _hqMixIn.getJMSSession();
        MessageProducer producer = session.createProducer(HornetQMixIn.getJMSQueue(QUEUE_IN));
        Message message = _hqMixIn.createJMSMessage(command);
        producer.send(message);
        session.close();

        session = _hqMixIn.createJMSSession();
        MessageConsumer consumer = session.createConsumer(HornetQMixIn.getJMSQueue(QUEUE_OUT_A));
        ObjectMessage msg = ObjectMessage.class.cast(consumer.receive(30000));
        Assert.assertEquals(command, msg.getObject());
        Assert.assertNull(consumer.receive(1000));
        
        consumer = session.createConsumer(HornetQMixIn.getJMSQueue(QUEUE_OUT_B));
        msg = ObjectMessage.class.cast(consumer.receive(1000));
        Assert.assertEquals(command, msg.getObject());
        msg = ObjectMessage.class.cast(consumer.receive(1000));
        Assert.assertEquals(command, msg.getObject());
        msg = ObjectMessage.class.cast(consumer.receive(1000));
        Assert.assertEquals(command, msg.getObject());
        msg = ObjectMessage.class.cast(consumer.receive(1000));
        Assert.assertEquals(command, msg.getObject());
        Assert.assertNull(consumer.receive(1000));
        
        consumer = session.createConsumer(HornetQMixIn.getJMSQueue(QUEUE_OUT_C));
        msg = ObjectMessage.class.cast(consumer.receive(1000));
        Assert.assertEquals(command, msg.getObject());
        msg = ObjectMessage.class.cast(consumer.receive(1000));
        Assert.assertEquals(command, msg.getObject());
        msg = ObjectMessage.class.cast(consumer.receive(1000));
        Assert.assertEquals(command, msg.getObject());
        msg = ObjectMessage.class.cast(consumer.receive(1000));
        Assert.assertEquals(command, msg.getObject());
        Assert.assertNull(consumer.receive(1000));
        session.close();
    }

    @Test
    public void testRollbackB() throws Exception {
        String command = "rollback.B";
        
        Session session = _hqMixIn.getJMSSession();
        MessageProducer producer = session.createProducer(HornetQMixIn.getJMSQueue(QUEUE_IN));
        Message message = _hqMixIn.createJMSMessage(command);
        producer.send(message);
        session.close();

        session = _hqMixIn.createJMSSession();
        MessageConsumer consumer = session.createConsumer(HornetQMixIn.getJMSQueue(QUEUE_OUT_A));
        ObjectMessage msg = ObjectMessage.class.cast(consumer.receive(1000));
        Assert.assertEquals(command, msg.getObject());
        Assert.assertNull(consumer.receive(1000));
        
        consumer = session.createConsumer(HornetQMixIn.getJMSQueue(QUEUE_OUT_B));
        msg = ObjectMessage.class.cast(consumer.receive(1000));
        Assert.assertEquals(command, msg.getObject());
        Assert.assertNull(consumer.receive(1000));
        
        consumer = session.createConsumer(HornetQMixIn.getJMSQueue(QUEUE_OUT_C));
        msg = ObjectMessage.class.cast(consumer.receive(1000));
        Assert.assertEquals(command, msg.getObject());
        Assert.assertNull(consumer.receive(1000));
        session.close();
    }
    
    @Test
    public void testNonTransacted() throws Exception {
        String command = "rollback.A";
        
        Session session = _hqMixIn.getJMSSession();
        MessageProducer producer = session.createProducer(HornetQMixIn.getJMSQueue(QUEUE_IN_NOTX));
        Message message = _hqMixIn.createJMSMessage(command);
        producer.send(message);
        session.close();

        session = _hqMixIn.createJMSSession();
        MessageConsumer consumer = session.createConsumer(HornetQMixIn.getJMSQueue(QUEUE_OUT_A));
        Assert.assertNull(consumer.receive(1000));
        consumer = session.createConsumer(HornetQMixIn.getJMSQueue(QUEUE_OUT_B));
        Assert.assertNull(consumer.receive(1000));
        consumer = session.createConsumer(HornetQMixIn.getJMSQueue(QUEUE_OUT_C));
        Assert.assertNull(consumer.receive(1000));
        session.close();
    }
}
