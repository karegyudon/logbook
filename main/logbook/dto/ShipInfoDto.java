/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.dto;

/**
 * 艦娘の名前と種別を表します
 *
 */
public final class ShipInfoDto extends AbstractDto {

    /** 空の艦種 */
    public static final ShipInfoDto EMPTY = new ShipInfoDto("", "", "");

    /** 名前 */
    private final String name;

    /** 艦種 */
    private final String type;

    /** 改レベル */
    private final int afterlv;

    /** flagshipもしくはelite (敵艦のみ) */
    private final String flagship;

    /**
     * コンストラクター
     */
    public ShipInfoDto(String name, String type, String flagship) {
        this.name = name;
        this.type = type;
        this.afterlv = 0;
        this.flagship = flagship;
    }

    /**
     * コンストラクター
     */
    public ShipInfoDto(String name, String type, int afterlv, String flagship) {
        this.name = name;
        this.type = type;
        this.afterlv = afterlv;
        this.flagship = flagship;
    }

    /**
     * コンストラクター
     */
    public ShipInfoDto(String name, String type) {
        this.name = name;
        this.type = type;
        this.afterlv = 0;
        this.flagship = "";
    }

    /**
     * @return 名前
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return 艦種
     */
    public String getType() {
        return this.type;
    }

    /**
     * @return 改造レベル(改造ができない場合、0)
     */
    public int getAfterlv() {
        return this.afterlv;
    }

    /**
     * @return flagshipもしくはelite
     */
    public String getFlagship() {
        return this.flagship;
    }
}