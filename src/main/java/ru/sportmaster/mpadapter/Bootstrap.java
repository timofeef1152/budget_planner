package ru.sportmaster.mpadapter;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Bootstrap {

    public static void main(String[] args) {
        // Запускаем Spring Boot без веб-сервера
        ConfigurableApplicationContext context = new SpringApplicationBuilder(Bootstrap.class).web(
                        WebApplicationType.NONE) // Отключаем веб-сервер
                .headless(false) // Разрешаем GUI
                .run(args);

        PlannerRepository plannerRepository = context.getBean(PlannerRepository.class);
        List<Planner> planners = plannerRepository.findAll();

        Planner planner;
        if (planners.isEmpty()) {
            planner = new Planner(new ArrayList<>());
            plannerRepository.save(planner);
        } else {
            planner = planners.getFirst();
        }

        // Запускаем Swing UI в EDT
        SwingUtilities.invokeLater(() -> {
            BudgetSwingUI ui = new BudgetSwingUI(planner,
                                                 context.getBean(GetPlannerUseCase.class),
                                                 context.getBean(AddStoryUseCase.class),
                                                 context.getBean(DeleteStoryUseCase.class),
                                                 context.getBean(CalculateBalanceForDateUseCase.class),
                                                 context.getBean(UpdateStoryDateUseCase.class),
                                                 context.getBean(UpdateStoryAmountUseCase.class),
                                                 context.getBean(UpdateStoryCommentUseCase.class));
            ui.setVisible(true);
        });
    }

    @Bean
    public CommandLineRunner demo(PlannerRepository repository) {
        return (args) -> {
            // Инициализационные данные если нужно
            System.out.println("Application started successfully!");
        };
    }
}
