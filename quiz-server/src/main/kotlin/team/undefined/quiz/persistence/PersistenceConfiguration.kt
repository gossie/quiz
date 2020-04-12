package team.undefined.quiz.persistence

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.connectionfactory.init.CompositeDatabasePopulator
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator

@Configuration
class PersistenceConfiguration {

    @Bean
    fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)

        val populator = CompositeDatabasePopulator()
        populator.addPopulators(ResourceDatabasePopulator(ClassPathResource("schema.sql")))
        initializer.setDatabasePopulator(populator)

        return initializer
    }

}