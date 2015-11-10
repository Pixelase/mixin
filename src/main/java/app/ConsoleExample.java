package app;

@Mixin(impl = DancerImpl.class)
interface Dancer {
	void dance();
}

@Mixin(impl = JumperImpl.class)
interface Jumper {
	void jump();
}

class DancerImpl implements Dancer {
	public void dance() {
		System.out.println("Dancing...");
	}
}

class JumperImpl implements Jumper {
	public void jump() {
		System.out.println("Jumping...");
	}
}

abstract class MyMixin implements Dancer, Jumper {
	public void newMethod() {
		System.out.println("New method from MyMixin");
	}
}

public class ConsoleExample {
	public static void main(String[] args) {
		MyMixin myMixin = MixinBuilder.newInstance(MyMixin.class);
		myMixin.dance();
		myMixin.jump();
		myMixin.newMethod();
	}
}
