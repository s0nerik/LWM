package app.helper.db
import groovy.transform.CompileStatic

@CompileStatic
class SortOrder {
    public static final SortOrder RANDOM = new RandomSortOrder()

    private final Map<String, Order> order

    SortOrder(Map<String, Order> order) {
        this.order = order
    }

    SortOrder(List<String> columns, Order order) {
        this.order = [:]
        columns.each {
            this.order[it] = order
        }
    }

    @Override
    String toString() {
        order?.collect {
            switch (it.value) {
                case Order.ASCENDING:
                    return it.key + " ASC"
                case Order.DESCENDING:
                    return it.key + " DESC"
                default:
                    return ""
            }
        }?.join(", ")
    }
}

@CompileStatic
class RandomSortOrder extends SortOrder {
    RandomSortOrder() {
        super([:])
    }

    @Override
    String toString() { "random()" }
}