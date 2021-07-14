package com.vetrack.vetrack.TensorFlow;

public interface VdrSpeed {

    class Prediction {

        int id;
        float speed;

        public Prediction(int id, float speed) {
            this.id = id;
            this.speed = speed;
        }

        public int getId() {
            return id;
        }

        public float getSpeed() {
            return speed;
        }
    }

    Prediction predict(float[] acc);
}
