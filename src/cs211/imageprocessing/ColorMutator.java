package cs211.imageprocessing;

/**
 * Created by mukel on 6/12/15.
 */
public interface ColorMutator {
    int mutate(int color);

    default ColorMutator andThen(ColorMutator mutator) {
        return color -> mutator.mutate(mutate(color));
    }
}
