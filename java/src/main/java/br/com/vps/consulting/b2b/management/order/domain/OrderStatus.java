package br.com.vps.consulting.b2b.management.order.domain;

public enum OrderStatus {

    PENDING {
        @Override
        public boolean canTransitionTo(OrderStatus next) {
            return next == APPROVED || next == CANCELED;
        }
    },
    APPROVED {
        @Override
        public boolean canTransitionTo(OrderStatus next) {
            return next == IN_PROCESS || next == CANCELED;
        }
    },
    IN_PROCESS {
        @Override
        public boolean canTransitionTo(OrderStatus next) {
            return next == SENT || next == CANCELED;
        }
    },
    SENT {
        @Override
        public boolean canTransitionTo(OrderStatus next) {
            return next == DELIVERED || next == CANCELED;
        }
    },
    DELIVERED {
        @Override
        public boolean canTransitionTo(OrderStatus next) {
            return false;
        }
    },
    CANCELED {
        @Override
        public boolean canTransitionTo(OrderStatus next) {
            return false;
        }
    };

    public abstract boolean canTransitionTo(OrderStatus next);

}
