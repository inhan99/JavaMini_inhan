package HorseRace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class HorsePanel extends JPanel {
    private static final int FINISH_LINE = 1000;
    private static final int HORSE_W = 100, HORSE_H = 60;
    private static final Image HORSE_IMG; 
    static {
        // 원본은 resources/images/horse_top.png 라고 가정
        Image raw = new ImageIcon(
            HorsePanel.class.getResource("/images/horse.png")).getImage();
        HORSE_IMG = raw.getScaledInstance(
            HORSE_W, HORSE_H, Image.SCALE_SMOOTH);   // 한 번만 리사이즈
    }

    private final List<Horse> horses = new ArrayList<>();
    private final List<Horse> finishOrder = new ArrayList<>();
    private javax.swing.Timer timer;
    private Image backgroundImage;
    private boolean raceOngoing = false;

    interface RaceFinishListener { void onFinish(String winnerName); }
    private RaceFinishListener finishListener;

    public void setFinishListener(RaceFinishListener listener) { this.finishListener = listener; }

    HorsePanel() {
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/images/background.png")).getImage();
        } catch (Exception e) {
            System.out.println("배경 이미지 로딩 실패: " + e.getMessage());
        }
    }

    public void initRace(List<HorseSpec> specs) {
        horses.clear();
        finishOrder.clear();

        int startY = 160;
        int laneHeight = 60;
        for (int i = 0; i < specs.size(); i++) {
            int y = startY + i * laneHeight;
            horses.add(new BasicHorse(i + 1, specs.get(i), y));
        }
        repaint();
    }

    public void startRace() {
        if (raceOngoing) return;
        raceOngoing = true;

        timer = new javax.swing.Timer(30, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Horse horse : horses) {
                    if (horse.getX() < FINISH_LINE) {
                        horse.run();
                        if (horse.getX() >= FINISH_LINE && !finishOrder.contains(horse)) {
                            finishOrder.add(horse);
                            if (finishListener != null && finishOrder.size() == 1) {
                                finishListener.onFinish(horse.getName());
                            }
                        }
                    }
                }
                repaint();

                if (finishOrder.size() == horses.size()) {
                    timer.stop();
                    raceOngoing = false;
                }
            }
        });
        timer.start();
    }

    public void stopRace() {
        if (timer != null) timer.stop();
        raceOngoing = false;
        repaint();
    }

    boolean isRaceOngoing() { return raceOngoing; }
    List<Horse> getFinishOrder() { return Collections.unmodifiableList(finishOrder); }
    List<Horse> getHorses() { return Collections.unmodifiableList(horses); }

    // paint 
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        for (Horse h : horses) {
            int x = (int) h.getX();
            int y = h.getY();

            // 1️⃣ 말 그리기 (투명 PNG라 배경을 가리지 않음)
            g2.drawImage(HORSE_IMG, x, y, null);

            g2.setColor(Color.PINK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(h.getName(), x + 4, y +9);         
        }
        g2.dispose();
    }
    }

