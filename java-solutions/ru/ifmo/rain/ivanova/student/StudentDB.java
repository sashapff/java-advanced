package ru.ifmo.rain.ivanova.student;

import info.kgeorgiy.java.advanced.student.AdvancedStudentGroupQuery;
import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements AdvancedStudentGroupQuery {
    private static final Comparator<Student> COMPARATOR_BY_NAME =
            Comparator.comparing(Student::getLastName)
                    .thenComparing(Student::getFirstName)
                    .thenComparingInt(Student::getId);

    private static final Comparator<Student> COMPARATOR_BY_ID =
            Comparator.comparingInt(Student::getId);

    private static final Comparator<Group> COMPARATOR_GROUP_BY_NAME =
            Comparator.comparing(Group::getName);

    private Stream<Map.Entry<String, List<Student>>> getGroupsStreamBy(Stream<Student> students) {
        return students
                .collect(Collectors.groupingBy(Student::getGroup))
                .entrySet()
                .stream();
    }

    private Stream<Map.Entry<String, List<Student>>> getSortGroupsStreamBy(Collection<Student> students,
                                                                           Comparator<Student> comparator) {
        return getGroupsStreamBy(students.stream().sorted(comparator));

    }

    private List<Group> getGroupsBy(Collection<Student> students, Comparator<Student> comparator) {
        return getSortGroupsStreamBy(students, comparator)
                .map(entry -> new Group(entry.getKey(), entry.getValue()))
                .sorted(COMPARATOR_GROUP_BY_NAME)
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsBy(students, COMPARATOR_BY_NAME);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsBy(students, COMPARATOR_BY_ID);
    }

    private class Pair<E, T> {
        E first;
        T second;

        Pair(E first, T second) {
            this.first = first;
            this.second = second;
        }

        E getFirst() {
            return first;
        }

        T getSecond() {
            return second;
        }
    }

    private String getLargestGroupBy(Collection<Student> students,
                                     Function<Map.Entry<String, List<Student>>, Pair<String, Integer>> comparator) {
        return getGroupsStreamBy(students.stream())
                .map(comparator)
                .max(Comparator.<Pair<String, Integer>>comparingInt(Pair::getSecond)
                        .thenComparing(Pair::getFirst, Collections.reverseOrder(String::compareTo)))
                .map(Pair::getFirst)
                .orElse("");
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getLargestGroupBy(students, entry -> new Pair<>(entry.getKey(), entry.getValue().size()));
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroupBy(students, entry -> new Pair<>(entry.getKey(),
                getDistinctFirstNames(entry.getValue()).size()));
    }

    private List<String> getListBy(List<Student> students, Function<Student, String> comparator) {
        return students.stream().map(comparator).collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getListBy(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getListBy(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return getListBy(students, Student::getGroup);
    }

    private String getFullName(Student student) {
        return student.getFirstName() + " " + student.getLastName();
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getListBy(students, this::getFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream()
                .min(COMPARATOR_BY_ID)
                .map(Student::getFirstName)
                .orElse("");
    }

    private Stream<Student> filterStudentsStreamBy(Collection<Student> students, Predicate<Student> filter) {
        return students.stream()
                .filter(filter);
    }

    private List<Student> sortAndCollect(Stream<Student> stream, Comparator<Student> comparator) {
        return stream.sorted(comparator)
                .collect(Collectors.toList());
    }

    private List<Student> filterAndSortStudentsBy(Collection<Student> students,
                                                  Predicate<Student> filter) {
        return sortAndCollect(filterStudentsStreamBy(students, filter), StudentDB.COMPARATOR_BY_NAME);
    }

    private List<Student> sortStudentsBy(Collection<Student> students, Comparator<Student> comparator) {
        return sortAndCollect(students.stream(), comparator);
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudentsBy(students, COMPARATOR_BY_ID);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudentsBy(students, COMPARATOR_BY_NAME);
    }

    private Predicate<Student> getPredicate(Function<Student, String> function, String expected) {
        return student -> function.apply(student).equals(expected);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return filterAndSortStudentsBy(students, getPredicate(Student::getFirstName, name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return filterAndSortStudentsBy(students, getPredicate(Student::getLastName, name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return filterAndSortStudentsBy(students, getPredicate(Student::getGroup, group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return filterStudentsStreamBy(students, getPredicate(Student::getGroup, group))
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo)));
    }

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(this::getFullName,
                        Collectors.mapping(Student::getGroup, Collectors.toSet())))
                .entrySet()
                .stream()
                .max(Map.Entry.<String, Set<String>>comparingByValue(Comparator.comparingInt(Set::size))
                        .thenComparing(Map.Entry.comparingByKey(String::compareTo)))
                .map(Map.Entry::getKey)
                .orElse("");
    }

    private List<String> getByIndices(Collection<Student> students, int[] indices,
                                      Function<Student, String> comparator) {
        return Arrays.stream(indices).mapToObj(new ArrayList<>(students)::get)
                .map(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] indices) {
        return getByIndices(students, indices, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return getByIndices(students, indices, Student::getLastName);
    }

    @Override
    public List<String> getGroups(Collection<Student> students, int[] indices) {
        return getByIndices(students, indices, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return getByIndices(students, indices, this::getFullName);
    }
}