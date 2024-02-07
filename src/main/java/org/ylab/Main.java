package org.ylab;

import org.ylab.cli.ConsoleCLI;
import org.ylab.controllers.AdminController;
import org.ylab.controllers.MeasurementController;
import org.ylab.controllers.PersonController;
import org.ylab.infrastructure.in.console.ConsoleReader;
import org.ylab.infrastructure.in.db.ConnectionAdapter;
import org.ylab.infrastructure.in.db.MigrationUtil;
import org.ylab.repositories.implementations.*;
import org.ylab.services.*;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        ConnectionAdapter connectionAdapter = new ConnectionAdapter();
        PersonRepo personRepo = new PersonRepo(connectionAdapter);
        OperationService operationUseCase = new OperationService(new OperationRepo(connectionAdapter));
        CounterTypeService counterTypeUseCase = new CounterTypeService(new CounterTypeRepo(connectionAdapter));
        CounterService counterUseCase = new CounterService(new CounterRepo(connectionAdapter) ,counterTypeUseCase);
        TokenService tokenService = new TokenService(new TokenRepo(connectionAdapter));
        MeasurementService measurementUseCase = new MeasurementService(
                new MeasurementRepo(connectionAdapter), operationUseCase, counterUseCase);
        PersonService personUseCase = new PersonService(
                new PasswordService(), personRepo, operationUseCase,
                tokenService);
        ConsoleCLI cli  = new ConsoleCLI(new PersonController(personUseCase),
                new MeasurementController(measurementUseCase, counterUseCase, tokenService),
                new AdminController(measurementUseCase, operationUseCase, personUseCase,
                        tokenService, counterTypeUseCase, counterUseCase),
                new ConsoleReader());

        MigrationUtil migrationUtil = new MigrationUtil(connectionAdapter);
            //запуск миграций
            migrationUtil.migrate();

        try {
            cli.run();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}