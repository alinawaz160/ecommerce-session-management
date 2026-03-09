package com.ecommerce.checkout.config;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import java.util.List;

/**
 * Cassandra config.
 *
 * Keyspace DDL (run once manually or via init script):
 *
 *   CREATE KEYSPACE IF NOT EXISTS ecommerce
 *   WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
 *
 *   CREATE TABLE IF NOT EXISTS ecommerce.orders (
 *     order_id     UUID,
 *     session_id   TEXT,
 *     status       TEXT,
 *     total_amount DECIMAL,
 *     shipping     TEXT,
 *     created_at   TIMESTAMP,
 *     updated_at   TIMESTAMP,
 *     PRIMARY KEY (order_id)
 *   );
 *
 *   CREATE TABLE IF NOT EXISTS ecommerce.order_items (
 *     order_id     UUID,
 *     product_id   TEXT,
 *     product_name TEXT,
 *     price        DECIMAL,
 *     quantity     INT,
 *     subtotal     DECIMAL,
 *     image_url    TEXT,
 *     PRIMARY KEY (order_id, product_id)
 *   );
 *
 * (Spring Data Cassandra with SchemaAction.CREATE_IF_NOT_EXISTS will handle this
 * automatically from the @Table entity annotations when the app starts.)
 */
@Configuration
@EnableCassandraRepositories(basePackages = "com.ecommerce.checkout.repository")
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Value("${spring.cassandra.keyspace-name}")
    private String keyspaceName;

    @Value("${spring.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.cassandra.port}")
    private int port;

    @Value("${spring.cassandra.local-datacenter}")
    private String localDatacenter;

    @Override
    protected String getKeyspaceName() {
        return keyspaceName;
    }

    @Override
    protected String getContactPoints() {
        return contactPoints;
    }

    @Override
    protected int getPort() {
        return port;
    }

    @Override
    protected String getLocalDataCenter() {
        return localDatacenter;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        return List.of(
            CreateKeyspaceSpecification
                .createKeyspace(keyspaceName)
                .ifNotExists()
                .with(KeyspaceOption.DURABLE_WRITES, true)
                .withSimpleReplication()
        );
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[]{"com.ecommerce.checkout.entity"};
    }
}
