import {Button, Col, Divider, Form, Grid, Input, Row, Space, Tag, Typography, message, theme} from "antd";
import {SaveOutlined} from "@ant-design/icons";
import axios from "axios";
import {FunctionComponent, useEffect, useMemo, useState} from "react";
import {PluginInfoResponse, StandardResponse} from "../index";

type PluginSettingsProps = {
    data: PluginInfoResponse;
}

type SitemapFormValues = {
    uriPath?: string;
    sitemapText?: string;
}

const PluginSettings: FunctionComponent<PluginSettingsProps> = ({data}) => {
    const {token} = theme.useToken();
    const screens = Grid.useBreakpoint();
    const isPhone = Boolean(screens.xs && !screens.sm);
    const isCompact = !screens.md;
    const [form] = Form.useForm<SitemapFormValues>();
    const [loading, setLoading] = useState(false);
    const [messageApi, contextHolder] = message.useMessage();
    const version = data.config.version || data.plugin.version;

    useEffect(() => {
        form.setFieldsValue({
            uriPath: data.config.uriPath || "",
            sitemapText: data.config.sitemapText || "",
        });
    }, [data.config, form]);

    const shellStyle = useMemo(() => ({
        maxWidth: 980,
        margin: "0 auto",
        padding: isPhone ? 12 : isCompact ? 16 : 24,
        color: token.colorText,
        boxSizing: "border-box" as const,
    }), [isCompact, isPhone, token]);

    const panelStyle = useMemo(() => ({
        padding: isPhone ? 16 : 24,
        border: `1px solid ${token.colorBorderSecondary}`,
        borderRadius: 8,
        background: token.colorBgContainer,
    }), [isPhone, token]);

    const submit = async (values: SitemapFormValues) => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                uriPath: values.uriPath || "",
                sitemapText: values.sitemapText || "",
            });
            const {data: response} = await axios.post<StandardResponse<unknown>>("update", params, {
                headers: {"Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"},
            });
            if (!response.success) {
                throw new Error(response.message || "保存失败");
            }
            messageApi.success("已保存");
        } catch (e) {
            messageApi.error(e instanceof Error ? e.message : "保存失败");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={shellStyle}>
            {contextHolder}
            <Space direction="vertical" size={20} style={{width: "100%"}}>
                <div style={{display: "flex", justifyContent: "space-between", gap: 16, flexWrap: "wrap"}}>
                    <Space direction="vertical" size={4}>
                        <Space wrap style={{maxWidth: "100%"}}>
                            <Typography.Title level={3} style={{margin: 0, fontSize: isPhone ? 20 : undefined}}>{data.plugin.name}</Typography.Title>
                            <Tag>v{version}</Tag>
                        </Space>
                        <Typography.Text type="secondary" style={{display: "block", maxWidth: "100%"}}>{data.plugin.desc}</Typography.Text>
                    </Space>
                </div>

                <div style={panelStyle}>
                    <Form form={form} layout="vertical" onFinish={submit} requiredMark={false}>
                        <Row gutter={[isCompact ? 12 : 16, 0]}>
                            <Col xs={24} md={12}>
                                <Form.Item label="文件存放地址" name="uriPath">
                                    <Input autoComplete="off"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24}>
                                <Form.Item label="说明文字" name="sitemapText">
                                    <Input.TextArea rows={4}/>
                                </Form.Item>
                            </Col>
                            <Col xs={24}>
                                <Form.Item label="插件 HTML">
                                    <Input.TextArea value={'<plugin name="sitemap" view="widget"/>'} readOnly rows={2}/>
                                </Form.Item>
                            </Col>
                            <Col xs={24}>
                                <Form.Item label="预览" style={{marginBottom: 0}}>
                                    <iframe
                                        title="站点地图预览"
                                        src="/p/sitemap/widget?preview=true"
                                        style={{height: 48, width: 256, maxWidth: "100%", border: 0}}
                                    />
                                </Form.Item>
                            </Col>
                        </Row>

                        <Divider/>

                        <Button type="primary" htmlType="submit" icon={<SaveOutlined/>} loading={loading} style={isPhone ? {width: "100%"} : undefined}>
                            保存
                        </Button>
                    </Form>
                </div>
            </Space>
        </div>
    );
};

export default PluginSettings;
