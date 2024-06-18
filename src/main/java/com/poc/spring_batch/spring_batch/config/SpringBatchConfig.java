package com.poc.spring_batch.spring_batch.config;
import com.poc.spring_batch.spring_batch.component.EmployeeProcessor;
import com.poc.spring_batch.spring_batch.component.EmployeeWriter;
import com.poc.spring_batch.spring_batch.entity.Employee;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;


@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {

//    @Value("classpath:org/springframework/batch/core/schema-drop-h2.sql")
//    private Resource dropRepositoryTables;
//
//    @Value("classpath:org/springframework/batch/core/schema-h2.sql")
//    private Resource dataRepositorySchema;

    @Bean
    public FlatFileItemReader<Employee> reader(){

        FlatFileItemReader<Employee> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/employee_data.csv"));
        itemReader.setName("employeeReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(linemapper());
        return itemReader;
    }


    public LineMapper<Employee> linemapper(){

        DefaultLineMapper<Employee> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id","name","username","gender","salary");
        BeanWrapperFieldSetMapper<Employee> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Employee.class);
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }

    @Bean
    public EmployeeProcessor processor(){
        return new EmployeeProcessor();
    }

    @Bean
    public EmployeeWriter writer(){
        return new EmployeeWriter();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager){
        return new StepBuilder("step1",jobRepository).<Employee,Employee>chunk(10,transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }


    @Bean(name="batchJob")
    public Job job(JobRepository jobRepository, PlatformTransactionManager transactionManager){
        return new JobBuilder("importEmployeeFromCsvToDb",jobRepository)
                .preventRestart()
                .start(step1(jobRepository,transactionManager))
                .build();
    }


    @Bean(name = "transactionManager")
    public PlatformTransactionManager getTransactionManager(){
        return new ResourcelessTransactionManager();
    }


    @Bean(name = "jobRepository")
    public JobRepository getJobRepository() throws Exception{
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(h2DataSource());
        factory.setTransactionManager(getTransactionManager());
        factory.afterPropertiesSet();
        return factory.getObject();
    }

//    public DataSourceInitializer dataSourceInitializer(DataSource dataSource){
//
//        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
//        databasePopulator.addScript(dropRepositoryTables);
//        databasePopulator.addScript(dataRepositorySchema);
//        databasePopulator.setIgnoreFailedDrops(false);
//        DataSourceInitializer initializer = new DataSourceInitializer();
//        initializer.setDataSource(dataSource);
//        initializer.setDatabasePopulator(databasePopulator);
//        return initializer;
//    }

    public DataSource h2DataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:springbatchdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("dhruvil");
        dataSource.setPassword("123456");
//        dataSourceInitializer(dataSource);
        return dataSource;
    }

}
