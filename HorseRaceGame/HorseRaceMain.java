package HorseRace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List; //충돌 
import java.util.stream.Collectors;



public class HorseRaceMain extends JFrame {
    private final HorsePanel horsePanel = new HorsePanel();
    private final JLabel moneyLabel = new JLabel();
    private final JTextField betField = new JTextField("50000", 6);
    private final JComboBox<BetType> betTypeCombo = new JComboBox<>(BetType.values());
    private final JCheckBox[] horseChecks = new JCheckBox[8];
    private final JLabel oddsLabel = new JLabel("배당 예측: -");
    private final JLabel resultLabel = new JLabel(" ");

    private int money = 500_000;
    private final Map<String, Integer> winCount = new HashMap<>();

    //배당률
    private static final Map<String, Double> ODDS = Map.ofEntries(
            Map.entry("늙은말", 6.5), Map.entry("젊은말", 5.5), Map.entry("노련한말", 5.8),
            Map.entry("광폭한말", 5.0), Map.entry("게으른말", 7.0), Map.entry("똑똑한말", 5.0),
            Map.entry("운좋은말", 6.0), Map.entry("기적의말", 15.0)
    );

    //말 스팩
    private static final List<HorseSpec> SPECS = List.of(
            new HorseSpec("늙은말", 2.0, 4.0, 0.10, 3),
            new HorseSpec("젊은말", 1.8, 4.2, 0.10, 3),
            new HorseSpec("노련한말", 2.2, 3.8, 0.10, 3),
            new HorseSpec("광폭한말", 1.9, 4.1, 0.10, 3),
            new HorseSpec("게으른말", 1.7, 4.3, 0.10, 3),
            new HorseSpec("똑똑한말", 2.0, 4.0, 0.10, 3),
            new HorseSpec("운좋은말", 1.8, 4.5, 0.08, 2),
            new HorseSpec("기적의말", 1.0, 2.0, 0.01,120)
    );

    public HorseRaceMain() {
        setTitle("🏇 경마 게임 ");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1500, 700);
        setLayout(new BorderLayout());
        setResizable(false);

        add(horsePanel, BorderLayout.CENTER);
        add(buildControlPanel(), BorderLayout.EAST);

        setVisible(true);
    }

    //UI
    private JPanel buildControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(320, 600));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        moneyLabel.setText("💰 현재 소지금: " + money + "원");
        panel.add(moneyLabel);
        panel.add(Box.createVerticalStrut(10));

        //배팅 타입
        JPanel typeRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typeRow.add(new JLabel("배팅 방식:"));
        betTypeCombo.setMaximumSize(new Dimension(120, 25));
        typeRow.add(betTypeCombo);
        panel.add(typeRow);

        //금액 입력
        JPanel betRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        betRow.add(new JLabel("금액:"));
        betField.setMaximumSize(new Dimension(120, 25));
        betRow.add(betField);
        panel.add(betRow);

        panel.add(oddsLabel);
        panel.add(Box.createVerticalStrut(10));

        JButton startBtn = new JButton("🏁 경주 시작");
        panel.add(startBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(resultLabel);

        //체크박스 말 선택
        panel.add(new JLabel("배팅할 말 선택:"));
        JPanel checkGrid = new JPanel(new GridLayout(0, 1, 3, 3));
        for (int i = 0; i < SPECS.size(); i++) {
            HorseSpec spec = SPECS.get(i);
            winCount.put(spec.name(), 0);
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.add(new JLabel(String.format("x%.1f", ODDS.get(spec.name()))));
            horseChecks[i] = new JCheckBox(spec.name());
            row.add(horseChecks[i]);
            checkGrid.add(row);
        }
        panel.add(checkGrid);

        //리스너
        Runnable oddsUpdater = () -> {
            List<String> picks = Arrays.stream(horseChecks)
                    .filter(AbstractButton::isSelected)
                    .map(AbstractButton::getText)
                    .collect(Collectors.toList());
            if (picks.isEmpty()) { oddsLabel.setText("배당 예측: -"); return; }
            double avg = picks.stream().mapToDouble(h -> ODDS.getOrDefault(h, 1.0)).average().orElse(1.0);
            oddsLabel.setText(String.format("배당 예측: x%.2f", avg));
        };

        for (JCheckBox cb : horseChecks) cb.addActionListener(e -> oddsUpdater.run());
        betTypeCombo.addActionListener(e -> oddsUpdater.run());

        startBtn.addActionListener(e -> onStartRace());

        return panel;
    }

    //게임 로직
    private void onStartRace() {
        // 선택 말 목록
        List<String> picks = Arrays.stream(horseChecks)
                .filter(AbstractButton::isSelected)
                .map(AbstractButton::getText)
                .collect(Collectors.toList());

        BetType type = (BetType) betTypeCombo.getSelectedItem();
        if (type == null) return;

        if (picks.size() != type.pickCount) {
            resultLabel.setText("⚠️ " + type.pickCount + "마리를 선택하세요 (" + type + ")");
            return;
        }

        int betAmount;
        try {
            betAmount = Integer.parseInt(betField.getText().trim());
        } catch (NumberFormatException e) {
            resultLabel.setText("⚠️ 숫자만 입력해주세요");
            return;
        }catch(Exception e) {
        	return;
        }

        if (betAmount <= 0 || betAmount > money) {
            resultLabel.setText("⚠️ 유효한 금액이 아닙니다");
            return;
        }

        // 경주 세팅 & 시작
        horsePanel.initRace(SPECS);
        resultLabel.setText("🏇 경주 중...");
        horsePanel.setFinishListener(winner -> handleResult(type, picks, betAmount));
        horsePanel.startRace();

        // 순위 팝업 표시용 타이머
        new javax.swing.Timer(100, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent evt) {
                if (!horsePanel.isRaceOngoing()) {
                    ((javax.swing.Timer) evt.getSource()).stop();
                    showRanking();
                }
            }
        }).start();
    }

    private void handleResult(BetType type, List<String> picks, int betAmount) {
        List<Horse> rank = horsePanel.getFinishOrder();
        String win1 = rank.get(0).getName();
        winCount.merge(win1, 1, Integer::sum);

        boolean isWin = switch (type) {
            case 단승식 -> picks.get(0).equals(rank.get(0).getName());
            case 복승식 -> picks.contains(rank.get(0).getName()) && picks.contains(rank.get(1).getName());
            case 쌍승식 -> picks.get(0).equals(rank.get(0).getName()) && picks.get(1).equals(rank.get(1).getName());
            case 삼복승식 -> rank.subList(0, 3).stream().map(Horse::getName).collect(Collectors.toSet()).containsAll(picks);
            case 삼쌍승식 -> picks.get(0).equals(rank.get(0).getName()) && picks.get(1).equals(rank.get(1).getName()) && picks.get(2).equals(rank.get(2).getName());
        };

        if (isWin) {
            double avgOdds = picks.stream().mapToDouble(ODDS::get).average().orElse(1.0);
            int gain = (int) (betAmount * avgOdds);
            money += gain;
            resultLabel.setText("🎉 적중! +" + gain + "원");
        } else {
            money -= betAmount;
            resultLabel.setText("😢실패");
        }
        moneyLabel.setText("💰 현재 소지금: " + money + "원");
    }

    private void showRanking() {
        StringBuilder sb = new StringBuilder("🏁 전체 순위 🏁\n");
        List<Horse> rank = horsePanel.getFinishOrder();
        for (int i = 0; i < rank.size(); i++) {
            sb.append(String.format("%d위: %s\n", i + 1, rank.get(i).getName()));
        }
        sb.append("\n🏆 누적 승수\n");
        winCount.forEach((n, c) -> sb.append(n).append(": ").append(c).append("승\n"));
        JOptionPane.showMessageDialog(this, sb.toString());
    }

    //main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(HorseRaceMain::new);
    }
}

